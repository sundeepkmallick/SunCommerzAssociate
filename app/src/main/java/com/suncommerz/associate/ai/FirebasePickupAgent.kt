package com.suncommerz.associate.ai

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
    private val runnerMutex = Mutex()
    private val sessionService = InMemorySessionService()
    @Volatile private var runner: InMemoryRunner? = null

    override suspend fun run(prompt: String, userId: String, sessionId: String): String {
        val activeRunner = getOrCreateRunner()
        val assistantTexts = mutableListOf<String>()
        activeRunner.runAsync(
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

    private suspend fun getOrCreateRunner(): InMemoryRunner {
        runner?.let { return it }
        return runnerMutex.withLock {
            runner?.let { return@withLock it }
            InMemoryRunner(
                agent = pickupAdkAgentFactory.createPickupAgent(firebaseModelProvider.getModel()),
                appName = "SunCommerzAssociatePickup",
                sessionService = sessionService,
            ).also { runner = it }
        }
    }
}
