package com.suncommerz.associate.ai

import com.sunmobile.aiagent.firebase.FirebaseAiModelFactory
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase AI Logic execution through the reusable SDK's ADK backend and host BOPIS tools. */
@Singleton
class SdkFirebasePickupAgent @Inject constructor(
    private val sdkPickupAgent: SdkPickupAgent,
) {
    suspend fun run(
        prompt: String,
        userId: String,
        sessionId: String,
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ): String = sdkPickupAgent.run(
        modelFactory = FirebaseAiModelFactory("gemini-3.5-flash-lite"),
        prompt = prompt,
        userId = userId,
        sessionId = sessionId,
        productId = productId,
        currentStoreId = currentStoreId,
        requestedQuantity = requestedQuantity,
        radiusMeters = radiusMeters,
    )
}
