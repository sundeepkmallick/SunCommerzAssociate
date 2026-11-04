package com.suncommerz.associate.ai

/** Backend-neutral contract for executing one pickup-agent request. */
interface PickupAgent {
    suspend fun run(prompt: String, userId: String, sessionId: String): String
}
