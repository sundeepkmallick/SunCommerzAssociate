package com.suncommerz.associate.ui.aipickup

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.InMemorySessionService
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import com.suncommerz.associate.ai.AIAgentManager
import com.suncommerz.associate.ai.HybridPickupAgentManager
import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.model.NotificationReasonType
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.usecase.notification.NotifyStoreManagerUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemPickedUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemUnavailableUseCase
import com.suncommerz.associate.domain.usecase.orderitem.SelectSubstituteUseCase
import com.suncommerz.associate.domain.usecase.orderitem.ReserveNearbyStoreUseCase
import com.suncommerz.associate.domain.usecase.storemanager.ObserveStoreManagerUseCase
import com.suncommerz.associate.domain.usecase.user.ObserveUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Clock

const val NEARBY_STORE_RADIUS = 3.00
const val MIN_QUANTITY_TO_INDICATE_LOW = 2

@HiltViewModel
class AIPickUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiAgentManager: AIAgentManager,
    private val hybridPickupAgentManager: HybridPickupAgentManager,
    private val orderRepository: OrderRepository,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeStoreManagerUseCase: ObserveStoreManagerUseCase,
    private val reserveNearbyStoreUseCase: ReserveNearbyStoreUseCase,
    private val markItemPickedUseCase: MarkItemPickedUseCase,
    private val selectSubstituteUseCase: SelectSubstituteUseCase,
    private val markItemUnavailableUseCase: MarkItemUnavailableUseCase,
    private val notifyStoreManagerUseCase: NotifyStoreManagerUseCase
) : ViewModel() {
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val assignedAssociateAndStoreManager =
        MutableStateFlow<Pair<StoreAssociate?, StoreManager?>?>(null)

    private val _uiState = MutableStateFlow<AIPickUpUiState>(AIPickUpUiState.Loading)
    val uiState: StateFlow<AIPickUpUiState> = _uiState.asStateFlow()

    init {
        getAssignedAssociateAndStoreManager()
        viewModelScope.launch {
            startAiPickup(orderId)
        }
    }

    private fun getAssignedAssociateAndStoreManager() {
        viewModelScope.launch {
            combine(
                observeUserUseCase(),
                observeStoreManagerUseCase()
            ) { user, manager ->
                user to manager
            }.collect {
                assignedAssociateAndStoreManager.value = it
            }
        }
    }

    fun startAiPickup(orderId: String) {
        viewModelScope.launch {
            _uiState.value = AIPickUpUiState.Processing(
                currentStatus = "Initializing AI...",
                processedItems = emptyList()
            )

            try {
                val (associate, _) = assignedAssociateAndStoreManager.filterNotNull().first()
                val agent = aiAgentManager.getRootOrchestratorAgent()
                val runner = InMemoryRunner(
                    agent = agent,
                    sessionService = InMemorySessionService()
                )
                val order = orderRepository.observeOrder(orderId).first()

                if (order == null) {
                    _uiState.value = AIPickUpUiState.Error(
                        message = "Order not found",
                        orderId = orderId
                    )
                    return@launch
                }

                val items = order.items
                if (items.isEmpty()) {
                    _uiState.value = AIPickUpUiState.ProposalReady(proposals = emptyList())
                    return@launch
                }

                val proposals = mutableListOf<AIPickUpItemProposal>()

                items.forEachIndexed { index, item ->
                    _uiState.update { state ->
                        val status = "Analyzing item ${index + 1}/${items.size}: ${item.product.name}..."
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(currentStatus = status, processedItems = proposals.toList())
                        } else {
                            AIPickUpUiState.Processing(
                                currentStatus = status,
                                processedItems = proposals.toList()
                            )
                        }
                    }

                    val prompt = """
                        Process this order item.

                        Order ID: $orderId
                        Product ID: ${item.product.id}
                        Required Quantity: ${item.requestedQuantity}
                        Current Store ID: ${order.store.id}
                        Nearby Radius (meters): $NEARBY_STORE_RADIUS
                        Minimum Quantity to indicate low stock: $MIN_QUANTITY_TO_INDICATE_LOW

                        Follow the system instructions and use the inventory tools.
                        Return exactly one valid pickup action.
                    """.trimIndent()

                    val sessionId = "pickup_${orderId}_${item.id}"
                    val response = hybridPickupAgentManager.runPickupItem(prompt) {
                        runFirebaseAgent(
                            runner = runner,
                            userId = associate?.id.orEmpty(),
                            sessionId = sessionId,
                            prompt = Content(
                                role = "user",
                                parts = listOf(Part(text = prompt))
                            )
                        )
                    }

                    Log.d(
                        "AI_PICKUP_DEBUG",
                        "Completed item ${item.product.id} using ${hybridPickupAgentManager.getCurrentBackend()}"
                    )
                    val proposal = parseAgentResponse(item = item, response = response)
                    proposals.add(proposal)

                    _uiState.update { state ->
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(
                                currentStatus = "Completed ${index + 1}/${items.size}",
                                processedItems = proposals.toList()
                            )
                        } else {
                            state
                        }
                    }
                }

                _uiState.value = AIPickUpUiState.ProposalReady(proposals = proposals.toList())
            } catch (e: Exception) {
                _uiState.value = AIPickUpUiState.Error(
                    message = e.message ?: "An unknown error occurred",
                    orderId = orderId
                )
            }
        }
    }

    private suspend fun runFirebaseAgent(
        runner: InMemoryRunner,
        userId: String,
        sessionId: String,
        prompt: Content
    ): String {
        val assistantTexts = mutableListOf<String>()

        runner.runAsync(
            userId = userId,
            sessionId = sessionId,
            invocationId = UUID.randomUUID().toString(),
            newMessage = prompt,
            stateDelta = emptyMap(),
            runConfig = RunConfig()
        ).collect { event ->
            Log.d("AI_PICKUP_DEBUG", "Firebase AI Event: $event")

            if (event.author == "PickupOrchestrator") {
                event.content?.parts
                    .orEmpty()
                    .mapNotNull { part -> part.text?.takeIf(String::isNotBlank) }
                    .forEach(assistantTexts::add)
            }
        }

        return assistantTexts.lastOrNull()
            ?: throw IllegalStateException("Firebase AI agent completed without an assistant text response")
    }

    private fun parseAgentResponse(
        item: OrderItem,
        response: String
    ): AIPickUpItemProposal {
        val normalized = response.trim().uppercase()

        return when {
            normalized == "PICKED" -> AIPickUpItemProposal(
                orderItemId = item.id,
                productName = item.product.name,
                proposal = ProposalType.PICKED,
                detail = ""
            )

            normalized == "UNAVAILABLE" -> AIPickUpItemProposal(
                orderItemId = item.id,
                productName = item.product.name,
                proposal = ProposalType.UNAVAILABLE,
                detail = ""
            )

            Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$").matches(normalized) -> {
                val storeId = Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$")
                    .matchEntire(normalized)!!
                    .groupValues[1]
                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.PICK_FROM_OTHER_STORE,
                    detail = storeId
                )
            }

            Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$").matches(normalized) -> {
                val productId = Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$")
                    .matchEntire(normalized)!!
                    .groupValues[1]
                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.SUBSTITUTE,
                    detail = productId
                )
            }

            else -> throw IllegalStateException(
                "AI agent returned an invalid pickup action for ${item.product.name}: $response"
            )
        }
    }

    fun approveProposals() {
        viewModelScope.launch {
            val currentState = _uiState.value as? AIPickUpUiState.ProposalReady ?: return@launch
            _uiState.value = currentState.copy(isUpdating = true)

            try {
                val order = orderRepository.observeOrder(orderId).first()
                if (order == null) {
                    _uiState.value = AIPickUpUiState.Error(
                        message = "Order not found",
                        orderId = orderId
                    )
                    return@launch
                }

                val (associate, manager) = assignedAssociateAndStoreManager
                    .filterNotNull()
                    .first()

                currentState.proposals.forEach { proposal ->
                    val orderItem = order.items.find { it.id == proposal.orderItemId }
                        ?: throw IllegalStateException(
                            "Order item ${proposal.orderItemId} no longer exists"
                        )

                    when (proposal.proposal) {
                        ProposalType.PICKED -> {
                            markItemPickedUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }

                        ProposalType.SUBSTITUTE -> {
                            selectSubstituteUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                substituteProductId = proposal.detail,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }

                        ProposalType.PICK_FROM_OTHER_STORE -> {
                            reserveNearbyStoreUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                nearbyStoreId = proposal.detail,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }

                        ProposalType.UNAVAILABLE -> {
                            markItemUnavailableUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                notifyManager = true,
                                notifyCustomer = true
                            )

                            notifyStoreManagerUseCase(
                                AssociateNotification(
                                    notificationReasonType = NotificationReasonType.INVENTORY_ISSUE_OUT_OF_STOCK,
                                    orderId = orderId,
                                    productId = orderItem.product.id,
                                    reportedByAssociateId = associate?.id.orEmpty(),
                                    reportedToManagerId = manager?.id.orEmpty(),
                                    reportDateTime = Clock.System.now()
                                )
                            )
                        }
                    }
                }

                _uiState.value = currentState.copy(isUpdating = false)
                _uiState.value = currentState.copy(isApprovedAndOrderUpdated = true)
            } catch (e: Exception) {
                _uiState.value = AIPickUpUiState.Error(
                    message = "Failed to update order: ${e.message ?: "Unknown error"}",
                    orderId = orderId
                )
            }
        }
    }
}
