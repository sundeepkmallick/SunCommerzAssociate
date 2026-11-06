package com.suncommerz.associate.ai

/**
 * Catalogue of LiteRT-LM models relevant to conversational pickup assistance.
 *
 * Google-published model support and approximate sizes are discovery metadata, not
 * proof that a particular artifact works with this app's ADK adapter or BOPIS tools.
 * Only entries with a pinned artifact, verified checksum, compatible app runtime,
 * and completed pickup-agent validation may be downloadable.
 */
data class LocalModelDefinition(
    val id: String,
    val displayName: String,
    val family: String,
    val description: String,
    val approximateSizeMb: Int,
    val artifactFileName: String,
    val downloadUrl: String?,
    /** Exact artifact size. Zero means the catalogue size is approximate only. */
    val sizeBytes: Long,
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
            sha256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c",
            minimumLiteRtLmVersion = "0.13.1",
            maximumTestedLiteRtLmVersion = "0.13.1",
            runtimeCompatibilityNote = "Verified with this app's LiteRT-LM 0.13.1 and ADK integration.",
            deviceCompatibilityNote = "App admission guardrails: Android API 26+, arm64, at least 8 GiB total RAM, 3 GiB currently available RAM, and 512 MiB free runtime-cache space. These are conservative app checks, not universal Google hardware guarantees.",
            licenseNote = "Gemma terms apply; review the upstream model card and terms before redistribution.",
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
            id = "gemma-4-e4b-it",
            displayName = "Gemma 4 E4B IT",
            family = "Gemma",
            description = "Larger instruction-tuned chat model; potentially stronger but heavier than E2B.",
            approximateSizeMb = 3_654,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Gemma terms apply; verify the exact artifact's model card and terms.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "gemma-3n-e2b-it",
            displayName = "Gemma 3n E2B IT",
            family = "Gemma",
            description = "Instruction-tuned multimodal-capable chat model in the E2B size class.",
            approximateSizeMb = 2_965,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Gemma terms apply; verify the exact artifact's model card and terms.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "gemma-3n-e4b-it",
            displayName = "Gemma 3n E4B IT",
            family = "Gemma",
            description = "Larger instruction-tuned multimodal-capable chat model.",
            approximateSizeMb = 4_235,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Gemma terms apply; verify the exact artifact's model card and terms.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "gemma3-1b-it",
            displayName = "Gemma 3 1B IT",
            family = "Gemma",
            description = "Compact instruction-tuned chat model.",
            approximateSizeMb = 1_005,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Gemma terms apply; verify the exact artifact's model card and terms.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "functiongemma-270m",
            displayName = "FunctionGemma 270M",
            family = "Gemma",
            description = "Base model specialised for function-calling research; not a general chat model.",
            approximateSizeMb = 289,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Gemma terms apply; verify the exact artifact's model card and terms.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = "Experimental candidate only. This is a base/function model rather than a standard instruction-tuned chat model; it needs a separate BOPIS tool-calling evaluation. $NOT_YET_DOWNLOADABLE",
        ),
        LocalModelDefinition(
            id = "phi-4-mini-instruct",
            displayName = "Phi-4 Mini Instruct",
            family = "Phi",
            description = "Instruction-tuned chat model; comparatively large for a mobile associate app.",
            approximateSizeMb = 3_906,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Verify the exact model card and license for the LiteRT-LM conversion.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "qwen2.5-1.5b-instruct",
            displayName = "Qwen2.5 1.5B Instruct",
            family = "Qwen",
            description = "Instruction-tuned chat model with a mid-sized mobile footprint.",
            approximateSizeMb = 1_598,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Verify the exact model card and license for the LiteRT-LM conversion.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
        ),
        LocalModelDefinition(
            id = "qwen3-0.6b",
            displayName = "Qwen3 0.6B",
            family = "Qwen",
            description = "Small chat model; multiple converted variants may have different runtime and memory needs.",
            approximateSizeMb = 586,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = "0.18.0",
            runtimeCompatibilityNote = "Google Android guidance demonstrates the standard CPU/GPU Qwen3-0.6B.litertlm artifact with LiteRT-LM 0.18.0. This is runtime guidance, not confirmation that this app's ADK adapter or BOPIS tools work with it.",
            deviceCompatibilityNote = "Google published a Qwen3-0.6B example benchmark on Vivo X300 Pro. This is a reference device, not a minimum requirement; check RAM, ABI, and performance on target handsets.",
            licenseNote = "The upstream Qwen3 model card lists Apache-2.0; verify the selected converted artifact's license and notices.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = "Approximate download size: 586 MB. Google Android guidance demonstrates a CPU/GPU Qwen3-0.6B.litertlm candidate with LiteRT-LM 0.18.0; app ADK compatibility is not yet verified. Device benchmark reference: Vivo X300 Pro, not a minimum requirement. Verify the exact converted artifact license. BOPIS tool calling and pinned checksum are pending; download is disabled.",
        ),
        LocalModelDefinition(
            id = "qwen2.5-0.5b-instruct",
            displayName = "Qwen2.5 0.5B Instruct",
            family = "Qwen",
            description = "Small instruction-tuned chat model.",
            approximateSizeMb = 521,
            artifactFileName = "Not pinned",
            downloadUrl = null,
            sizeBytes = 0L,
            sha256 = null,
            minimumLiteRtLmVersion = null,
            runtimeCompatibilityNote = RUNTIME_NOT_YET_VERIFIED,
            deviceCompatibilityNote = DEVICE_NOT_YET_VERIFIED,
            licenseNote = "Verify the exact model card and license for the LiteRT-LM conversion.",
            downloadable = false,
            pickupAgentValidated = false,
            minimumAndroidApi = null,
            requiresArm64 = null,
            minimumTotalRamBytes = null,
            minimumAvailableRamBytes = null,
            minimumRuntimeCacheBytes = null,
            availabilityStatusNote = NOT_YET_DOWNLOADABLE,
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
            model.sha256?.matches(Regex("(?i)^[a-f0-9]{64}$")) == true &&
            isRuntimeCompatible(model, runtimeVersion)

    fun downloadBlockReason(model: LocalModelDefinition, runtimeVersion: String): String {
        if (!model.downloadable || model.downloadUrl.isNullOrBlank() || model.sizeBytes <= 0L ||
            model.sha256?.matches(Regex("(?i)^[a-f0-9]{64}$")) != true
        ) return model.availabilityStatusNote
        val minimum = model.minimumLiteRtLmVersion
            ?: return "The minimum LiteRT-LM version for this exact artifact has not been verified."
        val runtime = parseVersion(runtimeVersion)
            ?: return "The app LiteRT-LM version ($runtimeVersion) could not be parsed."
        val required = parseVersion(minimum)
            ?: return "The model minimum LiteRT-LM version ($minimum) is not recorded in a supported format."
        if (runtime < required) return "This app packages LiteRT-LM $runtimeVersion; this model requires $minimum or newer."
        val maximum = model.maximumTestedLiteRtLmVersion?.let(::parseVersion)
        if (maximum != null && runtime > maximum) {
            return "This app packages LiteRT-LM $runtimeVersion; this exact artifact has only been tested through " +
                model.maximumTestedLiteRtLmVersion + ". Revalidate it before downloading."
        }
        return "Runtime requirement passes for LiteRT-LM $runtimeVersion. ADK adapter and BOPIS tool-calling validation are separate checks."
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
