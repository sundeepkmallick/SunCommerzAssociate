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
 * Runs only the associate's preferred backend. If that backend is not usable,
 * Firebase ADK is the safe fallback. Model management is intentionally kept
 * outside the AI Pickup screen and workflow.
 */
@Singleton
class HybridPickupAgentManager @Inject constructor(
    private val geminiNanoPickupAgent: GeminiNanoPickupAgent,
    private val liteRtLmPickupAgent: LiteRtLmPickupAgent,
    private val preferencesStore: AIModelPreferencesStore,
) {
    @Volatile
    private var activeBackend: AIBackend = AIBackend.FIREBASE

    suspend fun runPickupItem(
        prompt: String,
        userId: String,
        sessionId: String,
        firebaseFallback: suspend () -> String,
    ): String {
        when (preferencesStore.getPreferredBackend()) {
            AIBackend.GEMINI_NANO -> {
                try {
                    val result = geminiNanoPickupAgent.run(prompt)
                    check(isValidPickupAction(result.text)) {
                        "Gemini Nano returned an unsupported pickup action: " + result.text
                    }
                    activeBackend = AIBackend.GEMINI_NANO
                    Log.i(TAG, "AI Pickup used the preferred Gemini Nano backend")
                    return result.text
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    if (error is ThreadDeath) throw error
                    Log.w(TAG, "Preferred Gemini Nano backend unavailable; falling back to Firebase ADK", error)
                }
            }

            AIBackend.LITERT_LM -> {
                try {
                    val response = liteRtLmPickupAgent.run(prompt, userId, sessionId)
                    check(isValidPickupAction(response)) {
                        "LiteRT-LM returned an unsupported pickup action: " + response
                    }
                    activeBackend = AIBackend.LITERT_LM
                    Log.i(TAG, "AI Pickup used the preferred LiteRT-LM backend")
                    return response
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    if (error is ThreadDeath) throw error
                    Log.w(TAG, "Preferred LiteRT-LM backend unavailable; falling back to Firebase ADK", error)
                }
            }

            AIBackend.FIREBASE -> Unit
        }

        activeBackend = AIBackend.FIREBASE
        return firebaseFallback()
    }

    fun getCurrentBackend(): AIBackend = activeBackend

    fun getPreferredBackend(): AIBackend = preferencesStore.getPreferredBackend()

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
