package com.suncommerz.associate.ai

import android.content.Context
import android.os.Build
import com.google.mlkit.genai.agent.Agent
import com.google.mlkit.genai.agent.AgentEvent
import com.google.mlkit.genai.agent.AgentRunner
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Content
import com.google.mlkit.genai.prompt.Generation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the complete pickup reasoning and tool-calling loop with Gemini Nano.
 * AICore owns model provisioning; this app only checks whether the feature is
 * already available and never initiates a Gemini Nano model download.
 */
@Singleton
class GeminiNanoPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
) : PickupAgent {
    suspend fun availabilityStatus(): String {
        if (Build.VERSION.SDK_INT < 36) return "ANDROID_VERSION_UNSUPPORTED"
        val model = Generation.getClient()
        return try {
            model.checkStatus().toString()
        } finally {
            model.close()
        }
    }

    suspend fun isAvailable(): Boolean =
        availabilityStatus() == FeatureStatus.AVAILABLE.toString()

    override suspend fun run(prompt: String, userId: String, sessionId: String): String {
        check(isAvailable()) {
            "Gemini Nano is not currently available on this device"
        }

        val model = Generation.getClient()
        var runner: AgentRunner? = null

        try {
            val agent = Agent.builder()
                .setName("PickupOrchestrator")
                .setInstructions(PickupAgentInstructions.SYSTEM)
                .enableAppFunctions(true)
                .setMaxTurns(10)
                .build()

            runner = AgentRunner(context, agent)
            val invokedTools = mutableListOf<String>()
            var finalText: String? = null

            runner.runSingleTurnAsync(
                Content.builder()
                    .text(prompt)
                    .build()
            ).collect { event ->
                when (event) {
                    is AgentEvent.FunctionCall -> invokedTools += event.name
                    is AgentEvent.TextMessage -> {
                        if (!event.isPartial) finalText = event.text
                    }
                    else -> Unit
                }
            }

            val response = checkNotNull(finalText?.takeIf(String::isNotBlank)) {
                "Gemini Nano completed without a final pickup action"
            }
            check(invokedTools.any { name ->
                name.substringAfterLast('#')
                    .substringAfterLast('.')
                    .equals("getProductStockInStore", ignoreCase = true)
            }) {
                "Gemini Nano did not verify current-store stock with getProductStockInStore"
            }

            return response.trim()
        } finally {
            runner?.close()
            model.close()
        }
    }
}
