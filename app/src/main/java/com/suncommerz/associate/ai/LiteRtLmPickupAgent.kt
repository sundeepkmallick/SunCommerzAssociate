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
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ADK + LiteRT-LM execution for Gemma 4 E2B IT.
 *
 * Model installation is managed by AI Model Preferences. The runtime refuses
 * to load any file that the download manager has not size/hash-verified.
 */
@Singleton
class LiteRtLmPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiAgentManager: AIAgentManager,
    private val modelDownloadManager: LiteRtLmModelDownloadManager,
) {
    private val initializationMutex = Mutex()

    @Volatile
    private var runner: InMemoryRunner? = null

    @Volatile
    private var model: LiteRtLmModel? = null

    suspend fun run(prompt: String, userId: String, sessionId: String): String =
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

    private suspend fun getOrCreateRunner(): InMemoryRunner {
        runner?.let { return it }
        return initializationMutex.withLock {
            runner?.let { return@withLock it }

            ensureDeviceCanRunModel()
            check(modelDownloadManager.isInstalled()) {
                "Gemma 4 E2B is not installed and verified. Open AI Model Preferences to install it."
            }
            val modelFile = findModelFile()
            val localModel = LiteRtLmModel.create(
                EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath,
                ),
                name = modelFile.name,
            )

            try {
                localModel.engine.initialize()
                val localRunner = InMemoryRunner(
                    agent = aiAgentManager.createPickupAgent(localModel),
                    appName = "SunCommerzAssociatePickup",
                    sessionService = InMemorySessionService(),
                )
                model = localModel
                runner = localRunner
                localRunner
            } catch (error: Throwable) {
                runCatching { localModel.close() }
                throw error
            }
        }
    }

    private fun ensureDeviceCanRunModel() {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "LiteRT-LM requires Android 8.0 or later"
        }
        check(Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }) {
            "Gemma 4 E2B requires a compatible 64-bit ARM device"
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        check(!activityManager.isLowRamDevice) {
            "This device is classified as a low-RAM device"
        }

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val eightGiB = 8L * 1024L * 1024L * 1024L
        val threeGiB = 3L * 1024L * 1024L * 1024L
        check(memoryInfo.totalMem >= eightGiB) {
            "Gemma 4 E2B is intended for devices with approximately 8 GB RAM or more"
        }
        check(!memoryInfo.lowMemory && memoryInfo.availMem >= threeGiB) {
            "Not enough free memory to safely load Gemma 4 E2B"
        }
        check(context.cacheDir.usableSpace >= 512L * 1024L * 1024L) {
            "Not enough free storage for the LiteRT-LM runtime cache"
        }
    }

    private fun findModelFile(): File {
        val externalDirectory = context.getExternalFilesDir(null) ?: context.filesDir
        val modelFile = File(externalDirectory, MODEL_FILE_NAME)
        check(modelFile.isFile && modelFile.length() == MODEL_SIZE_BYTES) {
            "The verified Gemma 4 E2B model file is missing or has an unexpected size."
        }
        return modelFile
    }

    private companion object {
        const val MODEL_FILE_NAME = "gemma-4-E2B-it.litertlm"
        const val MODEL_SIZE_BYTES = 2_588_147_712L
    }
}
