package com.suncommerz.associate.ai

import android.util.Log
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

enum class AIBackend {
    GEMINI_NANO,
    LITERT_LM,
    FIREBASE,
}

/**
 * Selects the pickup agent in priority order: Gemini Nano, ADK + LiteRT-LM,
 * then Firebase ADK. All local tools are read-only; order mutations remain
 * behind the associate approval flow.
 */
@Singleton
class HybridPickupAgentManager @Inject constructor(
    private val geminiNanoPickupAgent: GeminiNanoPickupAgent,
    private val liteRtLmPickupAgent: LiteRtLmPickupAgent,
) {
    @Volatile private var nanoDisabledForProcess = false
    @Volatile private var liteRtDisabledForProcess = false
    @Volatile private var activeBackend: AIBackend = AIBackend.FIREBASE

    suspend fun runPickupItem(
        prompt: String,
        userId: String,
        sessionId: String,
        firebaseFallback: suspend () -> String,
    ): String {
        if (!nanoDisabledForProcess) {
            try {
                val result = geminiNanoPickupAgent.run(prompt)
                check(isValidPickupAction(result.text)) {
                    "Gemini Nano returned an unsupported pickup action: " + result.text
                }
                activeBackend = AIBackend.GEMINI_NANO
                Log.i(TAG, "AI Pickup selected Gemini Nano; tools=" + result.invokedTools)
                return result.text
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (error is ThreadDeath) throw error
                nanoDisabledForProcess = true
                Log.w(TAG, "Gemini Nano unavailable or failed; trying LiteRT-LM", error)
            }
        }

        if (!liteRtDisabledForProcess) {
            try {
                val response = liteRtLmPickupAgent.run(prompt, userId, sessionId)
                check(isValidPickupAction(response)) {
                    "LiteRT-LM returned an unsupported pickup action: " + response
                }
                activeBackend = AIBackend.LITERT_LM
                Log.i(TAG, "AI Pickup selected ADK + LiteRT-LM")
                return response
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (error is ThreadDeath) throw error
                liteRtDisabledForProcess = true
                Log.w(TAG, "ADK + LiteRT-LM unavailable or failed; falling back to Firebase", error)
            }
        }

        activeBackend = AIBackend.FIREBASE
        return firebaseFallback()
    }

    fun getCurrentBackend(): AIBackend = activeBackend

    fun isUsingLocalModel(): Boolean =
        activeBackend == AIBackend.GEMINI_NANO || activeBackend == AIBackend.LITERT_LM

    private fun isValidPickupAction(response: String): Boolean {
        val normalized = response.trim().uppercase()
        return normalized == "PICKED" ||
            normalized == "UNAVAILABLE" ||
            Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$").matches(normalized) ||
            Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$").matches(normalized)
    }

    private companion object {
        const val TAG = "HybridPickupAgent"
    }
}
