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

data class OnDevicePickupResult(
    val text: String,
    val invokedTools: List<String>,
)

/**
 * Runs the complete pickup reasoning and tool-calling loop with Gemini Nano.
 *
 * The API requires an eligible AICore device and Android AppFunctions support.
 * The model is considered usable only when it is already AVAILABLE; this class
 * deliberately does not download a model onto the device.
 */
@Singleton
class GeminiNanoPickupAgent @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun run(prompt: String): OnDevicePickupResult {
        check(Build.VERSION.SDK_INT >= 36) {
            "Gemini Nano AppFunction agent requires Android 16 or later"
        }

        val model = Generation.getClient()
        var runner: AgentRunner? = null

        try {
            check(model.checkStatus() == FeatureStatus.AVAILABLE) {
                "Gemini Nano is not currently available on this device"
            }

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
                        if (!event.isPartial) {
                            finalText = event.text
                        }
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

            return OnDevicePickupResult(
                text = response.trim(),
                invokedTools = invokedTools,
            )
        } finally {
            runner?.close()
            // GenerativeModel.close() is safe to call more than once.
            model.close()
        }
    }
}
