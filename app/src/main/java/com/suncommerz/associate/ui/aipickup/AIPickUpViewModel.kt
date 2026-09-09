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
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.ProductCategory
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.repository.OrderRepository
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

const val NEARBY_STORE_RADIUS = 3.00
const val MIN_QUANTITY_TO_INDICATE_LOW = 2
@HiltViewModel
class AIPickUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiAgentManager: AIAgentManager,
    private val orderRepository: OrderRepository,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeStoreManagerUseCase: ObserveStoreManagerUseCase
) : ViewModel() {
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val assignedAssociateAndStoreManager =
        MutableStateFlow<Pair<StoreAssociate?, StoreManager?>?>(null)

    private val _uiState = MutableStateFlow<AIPickUpUiState>(AIPickUpUiState.Loading)

    val uiState: StateFlow<AIPickUpUiState> = _uiState.asStateFlow()

    private val sessionService = InMemorySessionService()

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

            _uiState.value =
                AIPickUpUiState.Processing(
                    currentStatus = "Initializing AI...",
                    processedItems = emptyList()
                )

            try {
                val (associate, manager) = assignedAssociateAndStoreManager
                    .filterNotNull()
                    .first()

                val agent = aiAgentManager.getRootOrchestratorAgent()
                val runner = InMemoryRunner(
                    agent = agent,
                    sessionService = sessionService
                )

                val order = orderRepository.observeOrder(orderId).first()

                if (order == null) {
                    _uiState.value =
                        AIPickUpUiState.Error(message = "Order not found", orderId = orderId)
                    return@launch
                }

                val items = order.items
                if (items.isEmpty()) {
                    _uiState.value = AIPickUpUiState.ProposalReady(proposals = emptyList())
                    return@launch
                }

                val proposals = mutableListOf<AIPickUpItemProposal>()
                val sessionId = "pickup_$orderId"

                items.forEachIndexed { index, item ->
                    _uiState.update { state ->
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(
                                currentStatus = "Analyzing item ${index + 1}/${items.size}: ${item.product.name}...",
                                processedItems = proposals.toList()
                            )
                        } else {
                            AIPickUpUiState.Processing(
                                currentStatus = "Analyzing item ${index + 1}/${items.size}: ${item.product.name}...",
                                processedItems = proposals.toList()
                            )
                        }
                    }

                    val prompt = Content(
                        role = "user",
                        parts = listOf(
                            Part(
                                text =   """
                                            Process this Order.
                            
                                            Order ID: $orderId
                                            Product ID: ${item.product.id}
                                            Required Quantity: ${item.requestedQuantity}
                                            Current Store ID: ${order.store.id}
                                            Near By Radisu: $NEARBY_STORE_RADIUS
                                            Minimum Quantity to indicate low stock: $MIN_QUANTITY_TO_INDICATE_LOW
                            
                                            Return exactly one of:
                                            PICKED
                                            RESERVE_NEARBY_STORE(storeId)
                                            SUBSTITUTE(productId)
                                            UNAVAILABLE
                                        """.trimIndent()
                            )
                        )
                    )

                    val response = runAgent(
                        runner = runner,
                        userId = associate?.id.orEmpty(),
                        sessionId = sessionId,
                        prompt = prompt
                    )

                    val proposal = parseAgentResponse(
                        item = item,
                        response = response
                    )

                    proposals.add(proposal)

                    _uiState.update { state ->
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(
                                currentStatus =
                                    "Completed ${index + 1}/${items.size}",
                                processedItems =
                                    proposals.toList()
                            )
                        } else {
                            state
                        }
                    }
                }

                _uiState.value =
                    AIPickUpUiState.ProposalReady(
                        proposals = proposals.toList()
                    )

            } catch (e: Exception) {
                _uiState.value =
                    AIPickUpUiState.Error(
                        message = e.message ?: "An unknown error occurred",
                        orderId = orderId
                    )
            }
        }
    }

    private suspend fun runAgent(
        runner: InMemoryRunner,
        userId: String,
        sessionId: String,
        prompt: Content
    ): String {

        val responses = mutableListOf<String>()

        runner.runAsync(
            userId = userId,
            sessionId = sessionId,
            invocationId = UUID.randomUUID().toString(),
            newMessage = prompt,
            stateDelta = emptyMap(),
            runConfig = RunConfig()
        ).collect { event ->

            Log.d("AI_PICKUP_DEBUG", "AI Event: $event")

            val text = event.toString()

            if (text.isNotBlank()) {
                responses.add(text)
            }
        }

        return responses.lastOrNull().orEmpty()
    }

    private fun parseAgentResponse(
        item: OrderItem,
        response: String
    ): AIPickUpItemProposal {

        val normalized = response.trim().uppercase()

        return when {
            normalized.contains("RESERVE_NEARBY_STORE") -> {

                val storeId = extractValue(
                    normalized,
                    "RESERVE_NEARBY_STORE"
                )

                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.PICK_FROM_OTHER_STORE,
                    detail = storeId
                )
            }

            normalized.contains("SUBSTITUTE") -> {

                val productId =
                    extractValue(
                        normalized,
                        "SUBSTITUTE"
                    )

                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.SUBSTITUTE,
                    detail = productId
                )
            }

            normalized.contains("PICKED") -> {

                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.PICKED,
                    detail = ""
                )
            }

            else -> {

                AIPickUpItemProposal(
                    orderItemId = item.id,
                    productName = item.product.name,
                    proposal = ProposalType.UNAVAILABLE,
                    detail = ""
                )
            }
        }
    }

    private fun extractValue(
        response: String,
        command: String
    ): String {

        val regex =
            Regex(
                "$command\\(([^)]+)\\)"
            )

        return regex
            .find(response)
            ?.groupValues
            ?.getOrNull(1)
            .orEmpty()
    }


    fun approveProposals() {

        viewModelScope.launch {

            val currentState =
                _uiState.value
                        as? AIPickUpUiState.ProposalReady
                    ?: return@launch

            _uiState.value =
                currentState.copy(
                    isUpdating = true
                )

            try {
                val order = orderRepository.observeOrder(orderId).first()

                if (order == null) {
                    _uiState.value =
                        AIPickUpUiState.Error(
                            message = "Order not found",
                            orderId = orderId
                        )
                    return@launch
                }

                currentState.proposals.forEach { proposal ->

                    val existingItem =
                        order.items.find {
                            it.id == proposal.orderItemId
                        }

                    if (existingItem == null) {
                        return@forEach
                    }

                    when (proposal.proposal) {

                        ProposalType.PICKED -> {

                            orderRepository.updateOrderItem(
                                orderId = orderId,
                                item =
                                    existingItem.copy(
                                        pickupStatus =
                                            ItemPickupStatus.PICKED,
                                        pickedQuantity =
                                            existingItem.requestedQuantity
                                    )
                            )
                        }

                        ProposalType.SUBSTITUTE -> {

                            orderRepository.updateOrderItem(
                                orderId = orderId,
                                item =
                                    existingItem.copy(
                                        pickupStatus =
                                            ItemPickupStatus.SUBSTITUTE,
                                        pickedQuantity =
                                            existingItem.requestedQuantity,
                                        selectedSubstituteId =
                                            proposal.detail
                                    )
                            )
                        }

                        ProposalType.PICK_FROM_OTHER_STORE -> {
                            //TODO
                        }

                        ProposalType.UNAVAILABLE -> {

                            orderRepository.updateOrderItem(
                                orderId = orderId,
                                item =
                                    existingItem.copy(
                                        pickupStatus = ItemPickupStatus.UNAVAILABLE,
                                        pickedQuantity = 0
                                    )
                            )
                        }
                    }
                }

                _uiState.value =
                    currentState.copy(
                        isUpdating = false
                    )

            } catch (e: Exception) {

                _uiState.value =
                    AIPickUpUiState.Error(
                        message =
                            "Failed to update order: " +
                                    (e.message ?: "Unknown error"),
                        orderId = orderId
                    )
            }
        }
    }
}
