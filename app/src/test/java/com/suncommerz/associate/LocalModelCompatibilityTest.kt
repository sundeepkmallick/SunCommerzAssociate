package com.suncommerz.associate

import com.suncommerz.associate.ai.LocalModelCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelCompatibilityTest {
    @Test
    fun experimentalDownloadsAreNotBlockedByUnverifiedRuntimeCompatibility() {
        // Use entries that actually exist in the curated catalogue. Download eligibility
        // is intentionally independent from runtime validation: the UI warns that an
        // artifact/runtime combination is unverified, but does not silently block an
        // explicitly experimental download.
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)
        val qwen = LocalModelCatalog.get("qwen2.5-1.5b-instruct")

        assertTrue(LocalModelCatalog.canDownload(gemma, "0.13.1"))
        assertTrue(LocalModelCatalog.canDownload(gemma, "0.18.0"))
        assertTrue(LocalModelCatalog.canDownload(qwen, "0.17.9"))
        assertTrue(LocalModelCatalog.canDownload(qwen, "0.18.0"))
    }

    @Test
    fun entriesWithoutArtifactUrlsRemainUnavailable() {
        // Exercise the no-artifact guard with a real catalogue definition whose URL
        // is deliberately removed. Do not reference model IDs that are not in the
        // curated catalogue: get() correctly rejects unknown IDs.
        val gemmaWithoutUrl = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)
            .copy(downloadUrl = null)
        val qwenWithoutUrl = LocalModelCatalog.get("qwen2.5-1.5b-instruct")
            .copy(downloadUrl = "")

        assertFalse(LocalModelCatalog.canDownload(gemmaWithoutUrl, "0.18.0"))
        assertFalse(LocalModelCatalog.canDownload(qwenWithoutUrl, "0.18.0"))
    }

    @Test
    fun malformedRuntimeVersionIsRejected() {
        val gemma = LocalModelCatalog.get(LocalModelCatalog.DEFAULT_MODEL_ID)

        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "not-a-version"))
        assertFalse(LocalModelCatalog.isRuntimeCompatible(gemma, "0.13"))
    }
}
