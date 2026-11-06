package com.suncommerz.associate

import com.suncommerz.associate.ai.LocalModelCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelCompatibilityTest {
    @Test
    fun gemmaBaselineAllowsOnlyTheAppTestedRuntime() {
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)

        assertTrue(LocalModelCatalog.canDownload(gemma, "0.13.1"))
        assertFalse(LocalModelCatalog.canDownload(gemma, "0.13.0"))
        assertFalse(LocalModelCatalog.canDownload(gemma, "0.13.2"))
        assertFalse(LocalModelCatalog.canDownload(gemma, "0.18.0"))
    }

    @Test
    fun qwenRuntimeRequirementDoesNotBypassArtifactVerification() {
        val qwen = LocalModelCatalog.get("qwen3-0.6b")

        assertFalse(LocalModelCatalog.isRuntimeCompatible(qwen, "0.17.9"))
        assertTrue(LocalModelCatalog.isRuntimeCompatible(qwen, "0.18.0"))
        assertTrue(LocalModelCatalog.isRuntimeCompatible(qwen, "0.19.0"))
        assertFalse(LocalModelCatalog.canDownload(qwen, "0.18.0"))
    }

    @Test
    fun malformedRuntimeVersionIsRejected() {
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)

        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "not-a-version"))
        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "0.13"))
    }
}
