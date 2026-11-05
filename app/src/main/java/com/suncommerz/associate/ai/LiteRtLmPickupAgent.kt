package com.suncommerz.associate.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.litertlm.LiteRtLmModel
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.EngineConfig
import com.suncommerz.associate.ai.download.LiteRtLmModelDownloadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model-agnostic ADK + LiteRT-LM pickup execution.
 * The selected artifact and its compatibility metadata live in LocalModelCatalog.
 */
@Singleton
class LiteRtLmPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pickupAdkAgentFactory: PickupAdkAgentFactory,
    private val modelDownloadManager: LiteRtLmModelDownloadManager,
) : PickupAgent {
    private val runtimeMutex = Mutex()

    @Volatile private var runner: InMemoryRunner? = null
    @Volatile private var model: LiteRtLmModel? = null
    @Volatile private var loadedModelId: String? = null

    override suspend fun run(prompt: String, userId: String, sessionId: String): String =
        runtimeMutex.withLock {
            withContext(Dispatchers.IO) {
                val activeRunner = getOrCreateRunner()
                val assistantTexts = mutableListOf<String>()
                val invokedTools = mutableListOf<String>()

                activeRunner.runAsync(
                    userId = userId,
                    sessionId = sessionId,
                    invocationId = UUID.randomUUID().toString(),
                    newMessage = Content(role = "user", parts = listOf(Part(text = prompt))),
                    stateDelta = emptyMap(),
                    runConfig = RunConfig()
                ).collect { event ->
                    if (event.author == "PickupOrchestrator") {
                        event.content?.parts.orEmpty()
                            .mapNotNull { it.text?.takeIf(String::isNotBlank) }
                            .forEach(assistantTexts::add)
                    }
                    event.content?.parts.orEmpty()
                        .mapNotNull { it.functionCall?.name }
                        .forEach(invokedTools::add)
                }

                check(invokedTools.any {
                    it.substringAfterLast('.').equals("getProductStockInStore", ignoreCase = true)
                }) {
                    "LiteRT-LM agent did not verify stock with getProductStockInStore"
                }

                assistantTexts.lastOrNull()?.trim()?.takeIf(String::isNotBlank)
                    ?: throw IllegalStateException("LiteRT-LM agent completed without an assistant response")
            }
        }

    /** Release the native engine before replacing or deleting its model file. */
    suspend fun releaseModel() = runtimeMutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching { model?.close() }
            model = null
            runner = null
            loadedModelId = null
        }
    }

    private suspend fun getOrCreateRunner(): InMemoryRunner {
        val selected = modelDownloadManager.installedModelDefinition()
            ?: throw IllegalStateException("No verified LiteRT-LM model is installed. Open AI Model Preferences.")
        if (runner != null && loadedModelId == selected.id) return runner!!

        releaseNativeEngine()
        ensureDeviceCanRunModel(selected.id)
        val modelFile = modelDownloadManager.installedModelFile()
            ?: throw IllegalStateException("The installed local model is not verified.")
        val localModel = LiteRtLmModel.create(
            EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.absolutePath,
            ),
            name = selected.artifactFileName,
        )

        try {
            localModel.engine.initialize()
            val localRunner = InMemoryRunner(
                agent = pickupAdkAgentFactory.createPickupAgent(localModel),
                appName = "SunCommerzAssociatePickup",
                sessionService = InMemorySessionService(),
            )
            model = localModel
            runner = localRunner
            loadedModelId = selected.id
            return localRunner
        } catch (error: Throwable) {
            runCatching { localModel.close() }
            throw error
        }
    }

    private suspend fun releaseNativeEngine() {
        runCatching { model?.close() }
        model = null
        runner = null
        loadedModelId = null
    }

    private fun ensureDeviceCanRunModel(modelId: String) {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "LiteRT-LM requires Android 8.0 or later"
        }
        check(Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }) {
            "This LiteRT-LM model requires a compatible 64-bit ARM device"
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        check(!activityManager.isLowRamDevice) {
            "This device is classified as a low-RAM device"
        }
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        check(memoryInfo.totalMem >= MIN_DEVICE_RAM_BYTES) {
            "This local model is intended for devices with approximately 8 GB RAM or more"
        }
        check(!memoryInfo.lowMemory && memoryInfo.availMem >= MIN_AVAILABLE_RAM_BYTES) {
            "Not enough free memory to safely load this local model"
        }
        check(context.cacheDir.usableSpace >= MIN_RUNTIME_CACHE_BYTES) {
            "Not enough free storage for the LiteRT-LM runtime cache"
        }
        check(modelId == LocalModelCatalog.DEFAULT_MODEL_ID) {
            "This model has not yet passed this app's pickup-agent compatibility tests"
        }
    }

    private companion object {
        const val MIN_DEVICE_RAM_BYTES = 8L * 1024L * 1024L * 1024L
        const val MIN_AVAILABLE_RAM_BYTES = 3L * 1024L * 1024L * 1024L
        const val MIN_RUNTIME_CACHE_BYTES = 512L * 1024L * 1024L
    }
}
