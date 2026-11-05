package com.suncommerz.associate.ai

/**
 * Curated LiteRT-LM artifacts. Runtime compatibility is separate from pickup-agent
 * suitability: only entries explicitly marked downloadable may be installed.
 */
data class LocalModelDefinition(
    val id: String,
    val displayName: String,
    val family: String,
    val description: String,
    val artifactFileName: String,
    val downloadUrl: String?,
    val sizeBytes: Long,
    val sha256: String?,
    val minimumLiteRtLmVersion: String,
    val downloadable: Boolean,
    val availabilityNote: String,
)

object LocalModelCatalog {
    const val DEFAULT_MODEL_ID = "gemma-4-e2b-it"

    val models: List<LocalModelDefinition> = listOf(
        LocalModelDefinition(
            id = DEFAULT_MODEL_ID,
            displayName = "Gemma 4 E2B IT",
            family = "Gemma",
            description = "Google instruction-tuned chat model. Current app-verified LiteRT-LM artifact.",
            artifactFileName = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94/gemma-4-E2B-it.litertlm",
            sizeBytes = 2_588_147_712L,
            sha256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c",
            minimumLiteRtLmVersion = "0.13.1",
            downloadable = true,
            availabilityNote = "Available for this app's current LiteRT-LM and ADK dependency set.",
        ),
        LocalModelDefinition(
            id = "qwen3-0.6b",
            displayName = "Qwen3 0.6B",
            family = "Qwen",
            description = "Small chat model with a published LiteRT-LM artifact.",
            artifactFileName = "Qwen3-0.6B.litertlm",
            downloadUrl = null,
            sizeBytes = 614_000_000L,
            sha256 = null,
            minimumLiteRtLmVersion = "0.18.0",
            downloadable = false,
            availabilityNote = "Not enabled: its published artifact requires LiteRT-LM 0.18.0+, while this app is pinned to 0.13.1 for ADK compatibility. Pickup tool calling is also not yet validated.",
        ),
    )

    fun get(modelId: String): LocalModelDefinition =
        models.firstOrNull { it.id == modelId }
            ?: models.first { it.id == DEFAULT_MODEL_ID }
}
