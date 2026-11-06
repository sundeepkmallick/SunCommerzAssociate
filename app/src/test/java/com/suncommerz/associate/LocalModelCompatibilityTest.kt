package com.suncommerz.associate

import com.suncommerz.associate.ai.LocalModelCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelCompatibilityTest {
    @Test
    fun experimentalDownloadsAreNotBlockedByUnverifiedRuntimeCompatibility() {
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)
        val qwen = LocalModelCatalog.get("qwen3-0.6b")
        val functionGemma = LocalModelCatalog.get("functiongemma-270m")

        assertTrue(LocalModelCatalog.canDownload(gemma, "0.13.1"))
        assertTrue(LocalModelCatalog.canDownload(gemma, "0.18.0"))
        assertTrue(LocalModelCatalog.canDownload(qwen, "0.17.9"))
        assertTrue(LocalModelCatalog.canDownload(qwen, "0.18.0"))
        assertTrue(LocalModelCatalog.canDownload(functionGemma, "0.13.1"))
    }

    @Test
    fun entriesWithoutArtifactUrlsRemainUnavailable() {
        val gemma3n = LocalModelCatalog.get("gemma-3n-e2b-it")
        val qwenSmall = LocalModelCatalog.get("qwen2.5-0.5b-instruct")

        assertFalse(LocalModelCatalog.canDownload(gemma3n, "0.18.0"))
        assertFalse(LocalModelCatalog.canDownload(qwenSmall, "0.18.0"))
    }

    @Test
    fun malformedRuntimeVersionIsRejected() {
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)

        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "not-a-version"))
        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "0.13"))
    }
}
