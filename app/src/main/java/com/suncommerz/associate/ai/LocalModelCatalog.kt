package com.suncommerz.associate.ai

/**
 * Catalogue of LiteRT-LM models relevant to conversational pickup assistance.
 *
 * Google-published model support and approximate sizes are discovery metadata, not
 * proof that a particular artifact works with this app's ADK adapter or BOPIS tools.
 * Experimental download eligibility is separate from checksum, runtime, device, and
 * pickup-agent validation. Downloading does not approve a model for AI Pickup.
 */
data class LocalModelDefinition(
    val id: String,
    val displayName: String,
    val family: String,
    val description: String,
    val approximateSizeMb: Int,
    val artifactFileName: String,
    val downloadUrl: String?,
    /** Estimated/known transfer size in bytes; used for storage planning and progress fallback. */
    val sizeBytes: Long,
    /** Exact artifact size when authoritatively known; null means sizeBytes is only an estimate. */
    val expectedSizeBytes: Long? = null,
    val sha256: String?,
    /** Null means the minimum version for the exact artifact has not been verified. */
    val minimumLiteRtLmVersion: String?,
    /** Highest version explicitly tested for this exact artifact in this app; null means no upper bound recorded. */
    val maximumTestedLiteRtLmVersion: String? = null,
    val runtimeCompatibilityNote: String,
    val deviceCompatibilityNote: String,
    val licenseNote: String,
    val downloadable: Boolean,
    val pickupAgentValidated: Boolean,
    val minimumAndroidApi: Int?,
    val requiresArm64: Boolean?,
    val minimumTotalRamBytes: Long?,
    val minimumAvailableRamBytes: Long?,
    val minimumRuntimeCacheBytes: Long?,
    val availabilityStatusNote: String,
) {
    /** Complete user-facing note: estimated size, runtime, device, license, and release state. */
    val availabilityNote: String
        get() = listOf(
            "Approximate download size: $approximateSizeMb MB.",
            "LiteRT-LM compatibility: $runtimeCompatibilityNote",
            "Device compatibility: $deviceCompatibilityNote",
            "License: $licenseNote",
            availabilityStatusNote,
        ).joinToString(" ")
}

object LocalModelCatalog {
    const val DEFAULT_MODEL_ID = "gemma-4-e2b-it"

    private const val NOT_YET_DOWNLOADABLE =
        "Catalogue entry only. Download is disabled until the exact LiteRT-LM artifact, revision, file size, SHA-256, license, runtime compatibility, and BOPIS tool-calling behaviour have been verified."

    private const val DEVICE_NOT_YET_VERIFIED =
        "Google's published device benchmarks are reference results, not minimum requirements. Exact artifact ABI, Android version, peak memory, and performance must be checked on target devices before enabling installation."

    private const val RUNTIME_NOT_YET_VERIFIED =
        "Google lists this model in its LiteRT-LM supported-model catalogue. The minimum LiteRT-LM version for a specific downloadable artifact and compatibility with this app's ADK adapter have not yet been verified."

    val models: List<LocalModelDefinition> = listOf(
        LocalModelDefinition(
            id = DEFAULT_MODEL_ID,
            displayName = "Gemma 4 E2B IT",
            family = "Gemma",
            description = "Instruction-tuned chat model; current app-verified baseline.",
            approximateSizeMb = 2_583,
            artifactFileName = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94/gemma-4-E2B-it.litertlm",
            sizeBytes = 2_588_147_712L,
            expectedSizeBytes = 2_588_147_712L,
            sha256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c",
            minimumLiteRtLmVersion = "0.13.1",
            maximumTestedLiteRtLmVersion = "0.13.1",
            runtimeCompatibilityNote = "Verified with this app's LiteRT-LM 0.13.1 and ADK integration.",
            deviceCompatibilityNote = "App admission guardrails: Android API 26+, arm64, at least 8 GiB total RAM, 3 GiB currently available RAM, and 512 MiB free runtime-cache space. These are conservative app checks, not universal Google hardware guarantees.",
            licenseNote = "The LiteRT-LM package is marked Apache-2.0; the underlying Gemma weights remain subject to Google's Gemma terms and prohibited-use policy. Review both before redistribution.",
            downloadable = true,
            pickupAgentValidated = true,
            minimumAndroidApi = 26,
            requiresArm64 = true,
            minimumTotalRamBytes = 8L * 1024L * 1024L * 1024L,
            minimumAvailableRamBytes = 3L * 1024L * 1024L * 1024L,
            minimumRuntimeCacheBytes = 512L * 1024L * 1024L,
            availabilityStatusNote = "Available now. Exact artifact revision, byte size, and SHA-256 are pinned. This is the only catalogue model currently verified for this app's BOPIS pickup-agent flow.",
        ),
        LocalModelDefinition(
            id = "qwen2.5-1.5b-instruct",
            displayName = "Qwen2.5 1.5B Instruct",
            family = "Qwen",
            description = "Instruction-tuned chat model with a mid-sized mobile footprint.",
            approximateSizeMb = 1_598,
            artifactFileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/19edb84/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
            sizeBytes = 1_597_931_520L,
            expectedSizeBytes = 1_597_931_520L,
            sha256 = "faa60663b333290c1496c499828b21d3e3254a788cacd8cce917ce0f761a2dc9",
            minimumLiteRtLmVersion = "0.13.1",
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Verify the exact model card and license for the LiteRT-LM conversion.",
            downloadable = true,
            pickupAgentValidated = true,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = "Experimental download enabled. Artifact URL is pinned to file-upload revision 19edb84; exact byte size and SHA-256 are recorded. App runtime, ADK integration, device requirements, and BOPIS tool-calling validation remain unverified.",
        ),
    )

    /**
     * Conservative semantic-version gate. A model with no verified minimum version is
     * informational only. A recorded upper tested version blocks unverified newer runtimes.
     */
    fun isRuntimeCompatible(model: LocalModelDefinition, runtimeVersion: String): Boolean {
        val runtime = parseVersion(runtimeVersion) ?: return false
        val minimum = model.minimumLiteRtLmVersion?.let(::parseVersion) ?: return false
        if (runtime < minimum) return false
        val maximum = model.maximumTestedLiteRtLmVersion?.let(::parseVersion)
        return maximum == null || runtime <= maximum
    }

    fun canDownload(model: LocalModelDefinition, runtimeVersion: String): Boolean =
        model.downloadable && !model.downloadUrl.isNullOrBlank() && model.sizeBytes > 0L &&
            (model.sha256 == null || model.sha256.matches(Regex("(?i)^[a-f0-9]{64}$")))

    fun downloadBlockReason(model: LocalModelDefinition, runtimeVersion: String): String {
        if (model.downloadUrl.isNullOrBlank()) return "No downloadable artifact URL is recorded for this model."
        if (model.sizeBytes <= 0L) return "A usable download-size estimate is not recorded."
        if (model.sha256 == null) {
            return "Experimental download is allowed without a checksum; downloaded bytes will be marked unverified. Runtime, device, ADK, and BOPIS compatibility still need testing."
        }
        if (!model.sha256.matches(Regex("(?i)^[a-f0-9]{64}$"))) {
            return "The recorded checksum is malformed; correct it before downloading."
        }
        val minimum = model.minimumLiteRtLmVersion
        val maximum = model.maximumTestedLiteRtLmVersion
        val runtimeNote = when {
            minimum == null -> "The minimum runtime version is unknown."
            !isRuntimeCompatible(model, runtimeVersion) ->
                "The packaged runtime $runtimeVersion is outside the recorded runtime guidance ($minimum" +
                    (maximum?.let { " through $it" } ?: " or newer") + ")."
            else -> "The packaged runtime meets the recorded minimum $minimum."
        }
        return "Experimental download is enabled. $runtimeNote Downloading does not validate model loading, ADK integration, device suitability, or BOPIS tool calling."
    }

    private data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, Version::major, Version::minor, Version::patch)
    }

    private fun parseVersion(value: String): Version? {
        val match = Regex("^\\s*(\\d+)\\.(\\d+)\\.(\\d+)").find(value) ?: return null
        return runCatching {
            Version(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
        }.getOrNull()
    }
    fun get(modelId: String): LocalModelDefinition =
        requireNotNull(models.firstOrNull { it.id == modelId }) { "Unknown local model: $modelId" }
}
