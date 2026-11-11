package com.suncommerz.associate.ai

import android.content.Context
import com.sunmobile.aiagent.core.AgentBuilder
import com.sunmobile.aiagent.core.AgentConfiguration
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.mlkit.GeminiNanoAgentBackendProvider
import com.sunmobile.aiagent.mlkit.GeminiNanoAvailability
import com.sunmobile.aiagent.mlkit.GeminiNanoConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Nano pickup execution through SunMobileAiAgentSdk.
 *
 * The host continues to own AppFunction declarations, service implementation,
 * authorization and business operations. ML Kit Agent/Prompt lifecycle and
 * inference are encapsulated by the SDK backend.
 */
@Singleton
class GeminiNanoPickupAgent @Inject constructor(
    @ApplicationContext context: Context,
) : PickupAgent {
    private val backend = GeminiNanoAgentBackendProvider(
        context = context.applicationContext,
        configuration = GeminiNanoConfiguration(
            agentName = "PickupOrchestrator",
            instructions = PickupAgentInstructions.SYSTEM,
            maxTurns = 10,
            minimumAndroidApi = 36,
            enableAppFunctions = true,
        ),
    )

    suspend fun availabilityStatus(): String =
        when (val availability = backend.checkAvailability()) {
            GeminiNanoAvailability.Available -> "AVAILABLE"
            is GeminiNanoAvailability.ModelNotReady -> availability.featureStatus
            is GeminiNanoAvailability.UnsupportedAndroidVersion ->
                "UNSUPPORTED_ANDROID_API_${availability.actualApi}_REQUIRES_${availability.requiredApi}"
            is GeminiNanoAvailability.StatusCheckFailed ->
                "STATUS_CHECK_FAILED: ${availability.cause.message.orEmpty()}"
        }

    suspend fun isAvailable(): Boolean =
        backend.checkAvailability() == GeminiNanoAvailability.Available

    override suspend fun run(prompt: String, userId: String, sessionId: String): String {
        when (val availability = backend.checkAvailability()) {
            GeminiNanoAvailability.Available -> Unit
            is GeminiNanoAvailability.ModelNotReady ->
                error("Gemini Nano is not ready: ${availability.featureStatus}")
            is GeminiNanoAvailability.UnsupportedAndroidVersion ->
                error("Gemini Nano requires Android API ${availability.requiredApi}; device API is ${availability.actualApi}")
            is GeminiNanoAvailability.StatusCheckFailed ->
                throw IllegalStateException("Unable to check Gemini Nano readiness", availability.cause)
        }

        val agent = AgentBuilder()
            .configuration(
                AgentConfiguration(
                    name = "PickupOrchestrator",
                    instructions = PickupAgentInstructions.SYSTEM,
                    maxTurns = 10,
                    metadata = mapOf("application" to "SunCommerzAssociate"),
                )
            )
            .backend(backend)
            .build()

        return agent.use { runningAgent ->
            val result = runningAgent.execute(
                AgentRequest(
                    prompt = prompt,
                    sessionId = sessionId,
                    metadata = mapOf("userId" to userId),
                )
            )
            check(result.toolInvocations.any { invocation ->
                invocation.toolName.substringAfterLast('#')
                    .substringAfterLast('.')
                    .equals("getProductStockInStore", ignoreCase = true)
            }) {
                "Gemini Nano did not verify current-store stock with getProductStockInStore"
            }
            checkNotNull(result.text?.takeIf(String::isNotBlank)) {
                "Gemini Nano completed without a final pickup action"
            }.trim()
        }
    }
}