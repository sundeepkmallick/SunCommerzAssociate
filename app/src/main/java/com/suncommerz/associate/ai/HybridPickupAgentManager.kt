package com.suncommerz.associate.ai

import android.util.Log
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

enum class AIBackend { GEMINI_NANO, LITERT_LM, FIREBASE }

/**
 * Host-owned backend selection and fallback. Firebase and LiteRT-LM now execute through
 * SunMobileAiAgentSdk; Gemini Nano remains on the host AppFunctions implementation.
 */
@Singleton
class HybridPickupAgentManager @Inject constructor(
    private val geminiNanoPickupAgent: GeminiNanoPickupAgent,
    private val sdkLiteRtLmPickupAgent: SdkLiteRtLmPickupAgent,
    private val sdkFirebasePickupAgent: SdkFirebasePickupAgent,
    private val preferencesStore: AIModelPreferencesStore,
) {
    @Volatile private var activeBackend: AIBackend = AIBackend.FIREBASE

    suspend fun runPickupItem(
        prompt: String,
        userId: String,
        sessionId: String,
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ): String {
        val preferredBackend = preferencesStore.getPreferredBackend()
        if (preferredBackend == AIBackend.FIREBASE) {
            val response = runFirebase(prompt, userId, sessionId, productId, currentStoreId, requestedQuantity, radiusMeters)
            validateResponse(response, AIBackend.FIREBASE)
            activeBackend = AIBackend.FIREBASE
            return response
        }

        try {
            val response = when (preferredBackend) {
                AIBackend.GEMINI_NANO -> geminiNanoPickupAgent.run(prompt, userId, sessionId)
                AIBackend.LITERT_LM -> sdkLiteRtLmPickupAgent.run(
                    prompt, userId, sessionId, productId, currentStoreId, requestedQuantity, radiusMeters
                )
                AIBackend.FIREBASE -> error("Firebase is handled above")
            }
            validateResponse(response, preferredBackend)
            activeBackend = preferredBackend
            Log.i(TAG, "AI Pickup used the preferred ${preferredBackend.name} backend")
            return response
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.w(TAG, "Preferred ${preferredBackend.name} backend failed; falling back to SDK Firebase", error)
        }

        val fallbackResponse = runFirebase(prompt, userId, sessionId, productId, currentStoreId, requestedQuantity, radiusMeters)
        validateResponse(fallbackResponse, AIBackend.FIREBASE)
        activeBackend = AIBackend.FIREBASE
        return fallbackResponse
    }

    private suspend fun runFirebase(
        prompt: String,
        userId: String,
        sessionId: String,
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ) = sdkFirebasePickupAgent.run(
        prompt, userId, sessionId, productId, currentStoreId, requestedQuantity, radiusMeters
    )

    fun getCurrentBackend(): AIBackend = activeBackend
    fun getPreferredBackend(): AIBackend = preferencesStore.getPreferredBackend()
    fun isUsingLocalModel(): Boolean = activeBackend == AIBackend.GEMINI_NANO || activeBackend == AIBackend.LITERT_LM

    private fun validateResponse(response: String, backend: AIBackend) {
        val normalized = response.trim().uppercase()
        check(normalized == "PICKED" || normalized == "UNAVAILABLE" ||
            Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$").matches(normalized) ||
            Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$").matches(normalized)) {
            "${backend.name} returned an unsupported pickup action: $response"
        }
    }
    private companion object { const val TAG = "HybridPickupAgent" }
}
