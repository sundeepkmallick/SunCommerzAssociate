package com.suncommerz.associate.ai

import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.suncommerz.associate.ai.provider.FirebaseModelProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase Gemini execution through the shared ADK pickup-agent configuration. */
@Singleton
class FirebasePickupAgent @Inject constructor(
    private val pickupAdkAgentFactory: PickupAdkAgentFactory,
    private val firebaseModelProvider: FirebaseModelProvider,
) : PickupAgent {
    private val agentMutex = Mutex()

    @Volatile
    private var agent: LlmAgent? = null

    override suspend fun run(prompt: String, userId: String, sessionId: String): String {
        // Use a fresh runner/session service per request so a retry cannot inherit
        // conversation history from a previous processing attempt for the same item.
        val runner = InMemoryRunner(
            agent = getOrCreateAgent(),
            appName = "SunCommerzAssociatePickup",
            sessionService = InMemorySessionService(),
        )
        val assistantTexts = mutableListOf<String>()

        runner.runAsync(
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
        }

        return assistantTexts.lastOrNull()?.trim()?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException("Firebase pickup agent completed without an assistant response")
    }

    private suspend fun getOrCreateAgent(): LlmAgent {
        agent?.let { return it }
        return agentMutex.withLock {
            agent?.let { return@withLock it }
            pickupAdkAgentFactory.createPickupAgent(firebaseModelProvider.getModel())
                .also { agent = it }
        }
    }
}
