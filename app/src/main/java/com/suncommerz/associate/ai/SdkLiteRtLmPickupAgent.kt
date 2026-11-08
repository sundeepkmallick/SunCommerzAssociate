package com.suncommerz.associate.ai

import android.content.Context
import com.suncommerz.associate.ai.download.LiteRtLmModelDownloadManager
import com.sunmobile.aiagent.litertlm.LiteRtLmModelFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** LiteRT-LM pickup execution through the SDK; app retains model catalogue and verified download. */
@Singleton
class SdkLiteRtLmPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadManager: LiteRtLmModelDownloadManager,
    private val preferencesStore: AIModelPreferencesStore,
    private val sdkPickupAgent: SdkPickupAgent,
) {
    private val runtimeMutex = Mutex()

    /** Serialize model replacement/removal against active SDK inference turns. */
    suspend fun <T> withModelMaintenance(block: suspend () -> T): T = runtimeMutex.withLock { block() }

    suspend fun run(
        prompt: String,
        userId: String,
        sessionId: String,
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ): String = runtimeMutex.withLock {
        val definition = modelDownloadManager.installedModelDefinition()
            ?: error("No verified LiteRT-LM model is installed. Open AI Model Preferences.")
        check(definition.id == preferencesStore.getSelectedLocalModelId()) {
            "The selected local model is not the installed model. Install it before running AI Pickup."
        }
        val modelFile = modelDownloadManager.installedModelFile()
            ?: error("The installed local model file is missing or failed verification.")
        check(definition.pickupAgentValidated) {
            "This model has not passed this app's pickup-agent compatibility tests."
        }
        sdkPickupAgent.run(
            modelFactory = LiteRtLmModelFactory(
                modelFile = modelFile,
                cacheDirectory = File(context.cacheDir, "sunmobile-aiagent-litertlm"),
                modelName = definition.artifactFileName,
            ),
            prompt = prompt,
            userId = userId,
            sessionId = sessionId,
            productId = productId,
            currentStoreId = currentStoreId,
            requestedQuantity = requestedQuantity,
            radiusMeters = radiusMeters,
        )
    }
}
