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
 * The 2.6 GB model is not downloaded silently. Install the documented .litertlm
 * file first; absent weights, unsupported devices, low memory, and native runtime
 * initialization failures allow the dispatcher to continue to Firebase.
 */
@Singleton
class LiteRtLmPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiAgentManager: AIAgentManager,
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
        val candidates = listOf(
            File(externalDirectory, MODEL_FILE_NAME),
            File(context.filesDir, "models/$MODEL_FILE_NAME"),
        )
        return candidates.firstOrNull { it.isFile && it.length() >= MIN_MODEL_BYTES }
            ?: throw IllegalStateException(
                "Gemma 4 E2B model is not installed. Expected $MODEL_FILE_NAME in " +
                    externalDirectory.absolutePath
            )
    }

    private companion object {
        const val MODEL_FILE_NAME = "gemma-4-E2B-it.litertlm"
        const val MIN_MODEL_BYTES = 2_000_000_000L
    }
}
