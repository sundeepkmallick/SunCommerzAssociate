package com.suncommerz.associate.ai

import android.util.Log
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

enum class AIBackend {
    LOCAL,
    FIREBASE,
}

/**
 * Selects the complete pickup-agent engine, not just an LLM Model implementation.
 *
 * Gemini Nano and Firebase ADK use different agent/tool APIs, so each engine owns
 * its own orchestration while both call the same domain use cases. The Firebase
 * fallback is supplied by the caller so the existing ADK runner/session behavior
 * remains unchanged.
 */
@Singleton
class HybridPickupAgentManager @Inject constructor(
    private val geminiNanoPickupAgent: GeminiNanoPickupAgent,
) {
    @Volatile
    private var localDisabledForProcess = false

    @Volatile
    private var activeBackend: AIBackend = AIBackend.FIREBASE

    suspend fun runPickupItem(
        prompt: String,
        firebaseFallback: suspend () -> String,
    ): String {
        if (!localDisabledForProcess) {
            try {
                val result = geminiNanoPickupAgent.run(prompt)
                check(isValidPickupAction(result.text)) {
                    "Gemini Nano returned an unsupported pickup action: ${result.text}"
                }
                activeBackend = AIBackend.LOCAL
                Log.i(TAG, "AI Pickup selected Gemini Nano; tools=${result.invokedTools}")
                return result.text
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // All on-device pickup tools are read-only. Retrying this item with
                // Firebase cannot duplicate an order update; updates happen only
                // after the associate approves the proposals.
                localDisabledForProcess = true
                Log.w(TAG, "Gemini Nano unavailable or failed; falling back to Firebase", error)
            }
        }

        activeBackend = AIBackend.FIREBASE
        return firebaseFallback()
    }

    fun getCurrentBackend(): AIBackend = activeBackend

    fun isUsingLocalModel(): Boolean = activeBackend == AIBackend.LOCAL

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
