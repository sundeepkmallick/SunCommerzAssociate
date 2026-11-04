package com.suncommerz.associate.ui.aipickup

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.ai.HybridPickupAgentManager
import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.model.NotificationReasonType
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.usecase.notification.NotifyStoreManagerUseCase
import com.suncommerz.associate.domain.usecase.fullfilment.FindFulfillmentOptionsUseCase
import com.suncommerz.associate.domain.usecase.inventory.GetProductAvailabilityUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemPickedUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemStockLowUseCase
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

const val NEARBY_STORE_RADIUS = 3_000.0
const val MIN_QUANTITY_TO_INDICATE_LOW = 2

@HiltViewModel
class AIPickUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hybridPickupAgentManager: HybridPickupAgentManager,
    private val orderRepository: OrderRepository,
    private val getProductAvailabilityUseCase: GetProductAvailabilityUseCase,
    private val findFulfillmentOptionsUseCase: FindFulfillmentOptionsUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeStoreManagerUseCase: ObserveStoreManagerUseCase,
    private val reserveNearbyStoreUseCase: ReserveNearbyStoreUseCase,
    private val markItemPickedUseCase: MarkItemPickedUseCase,
    private val markItemStockLowUseCase: MarkItemStockLowUseCase,
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
        viewModelScope.launch { startAiPickup(orderId) }
    }

    private fun getAssignedAssociateAndStoreManager() {
        viewModelScope.launch {
            combine(observeUserUseCase(), observeStoreManagerUseCase()) { user, manager ->
                user to manager
            }.collect { assignedAssociateAndStoreManager.value = it }
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
                val order = orderRepository.observeOrder(orderId).first()

                if (order == null) {
                    _uiState.value = AIPickUpUiState.Error("Order not found", orderId)
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
                        val status = "Analyzing item " + (index + 1) + "/" + items.size +
                            ": " + item.product.name + "..."
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(currentStatus = status, processedItems = proposals.toList())
                        } else {
                            AIPickUpUiState.Processing(status, proposals.toList())
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

                    val sessionId = "pickup_" + orderId + "_" + item.id
                    val response = hybridPickupAgentManager.runPickupItem(
                        prompt = prompt,
                        userId = associate?.id.orEmpty(),
                        sessionId = sessionId,
                    )

                    Log.d(
                        "AI_PICKUP_DEBUG",
                        "Completed item " + item.product.id + " using " +
                            hybridPickupAgentManager.getCurrentBackend()
                    )
                    proposals.add(validateAgentResponse(item, order.store, response))

                    _uiState.update { state ->
                        if (state is AIPickUpUiState.Processing) {
                            state.copy(
                                currentStatus = "Completed " + (index + 1) + "/" + items.size,
                                processedItems = proposals.toList()
                            )
                        } else state
                    }
                }

                _uiState.value = AIPickUpUiState.ProposalReady(proposals.toList())
            } catch (e: Exception) {
                _uiState.value = AIPickUpUiState.Error(
                    message = e.message ?: "An unknown error occurred",
                    orderId = orderId
                )
            }
        }
    }

    /**
     * The model may suggest an action, but the manual fulfilment use case is the
     * source of truth. Re-check live inventory and only expose options that the
     * existing manual pickup flow would offer.
     */
    private suspend fun validateAgentResponse(
        item: OrderItem,
        currentStore: Store,
        response: String
    ): AIPickUpItemProposal {
        val localQuantity = getProductAvailabilityUseCase(
            productId = item.product.id,
            storeId = currentStore.id
        )
        if (localQuantity >= item.requestedQuantity) {
            return AIPickUpItemProposal(
                item.id, item.product.name, ProposalType.PICKED, ""
            )
        }

        val options = findFulfillmentOptionsUseCase(
            product = item.product,
            currentStore = currentStore,
            quantity = item.requestedQuantity,
            radiusMeters = NEARBY_STORE_RADIUS
        )
        val modelProposal = runCatching { parseAgentResponse(item, response) }.getOrNull()

        // Match the manual UI's priority: in-store substitutes before nearby stores.
        val substitutes = options.substitutesInCurrentStore.orEmpty()
        if (substitutes.isNotEmpty()) {
            val selected = substitutes.firstOrNull {
                modelProposal?.let { proposal -> proposal.detail == it.id && proposal.proposal == ProposalType.SUBSTITUTE } == true
            } ?: substitutes.first()
            return AIPickUpItemProposal(
                item.id, item.product.name, ProposalType.SUBSTITUTE, selected.id
            )
        }

        val nearbyStores = options.originalProductNearbyStores.orEmpty()
        if (nearbyStores.isNotEmpty()) {
            val selected = nearbyStores.firstOrNull {
                modelProposal?.let { proposal -> proposal.detail == it.id && proposal.proposal == ProposalType.PICK_FROM_OTHER_STORE } == true
            } ?: nearbyStores.first()
            return AIPickUpItemProposal(
                item.id, item.product.name, ProposalType.PICK_FROM_OTHER_STORE, selected.id
            )
        }

        return AIPickUpItemProposal(
            item.id, item.product.name, ProposalType.UNAVAILABLE, ""
        )
    }

    private fun parseAgentResponse(item: OrderItem, response: String): AIPickUpItemProposal {
        val normalized = response.trim().uppercase()
        return when {
            normalized == "PICKED" -> AIPickUpItemProposal(
                item.id, item.product.name, ProposalType.PICKED, ""
            )
            normalized == "UNAVAILABLE" -> AIPickUpItemProposal(
                item.id, item.product.name, ProposalType.UNAVAILABLE, ""
            )
            Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$").matches(normalized) -> {
                val storeId = Regex("^RESERVE_NEARBY_STORE\\(([^()\\s]+)\\)$")
                    .matchEntire(normalized)!!.groupValues[1]
                AIPickUpItemProposal(item.id, item.product.name, ProposalType.PICK_FROM_OTHER_STORE, storeId)
            }
            Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$").matches(normalized) -> {
                val productId = Regex("^SUBSTITUTE\\(([^()\\s]+)\\)$")
                    .matchEntire(normalized)!!.groupValues[1]
                AIPickUpItemProposal(item.id, item.product.name, ProposalType.SUBSTITUTE, productId)
            }
            else -> throw IllegalStateException(
                "AI agent returned an invalid pickup action for " + item.product.name + ": " + response
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
                    _uiState.value = AIPickUpUiState.Error("Order not found", orderId)
                    return@launch
                }

                val (associate, manager) = assignedAssociateAndStoreManager.filterNotNull().first()
                currentState.proposals.forEach { proposal ->
                    val orderItem = order.items.find { it.id == proposal.orderItemId }
                        ?: throw IllegalStateException("Order item " + proposal.orderItemId + " no longer exists")

                    val currentQuantity = getProductAvailabilityUseCase(
                        productId = orderItem.product.id,
                        storeId = order.store.id
                    )
                    val currentOptions = if (currentQuantity < orderItem.requestedQuantity) {
                        findFulfillmentOptionsUseCase(
                            product = orderItem.product,
                            currentStore = order.store,
                            quantity = orderItem.requestedQuantity,
                            radiusMeters = NEARBY_STORE_RADIUS
                        )
                    } else null

                    // Inventory can change between proposal generation and associate approval.
                    // Reject stale proposals instead of recording an invalid pickup/reservation.
                    when (proposal.proposal) {
                        ProposalType.PICKED -> {
                            check(currentQuantity >= orderItem.requestedQuantity) {
                                "Stock changed for " + orderItem.product.name + ". Run AI Pickup again."
                            }
                            markItemPickedUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }
                        ProposalType.SUBSTITUTE -> {
                            check(currentOptions?.substitutesInCurrentStore.orEmpty().any { it.id == proposal.detail }) {
                                "The selected substitute is no longer available for " + orderItem.product.name + ". Run AI Pickup again."
                            }
                            selectSubstituteUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                substituteProductId = proposal.detail,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }
                        ProposalType.PICK_FROM_OTHER_STORE -> {
                            check(currentOptions?.originalProductNearbyStores.orEmpty().any { it.id == proposal.detail }) {
                                "The selected nearby store no longer has enough stock for " + orderItem.product.name + ". Run AI Pickup again."
                            }
                            reserveNearbyStoreUseCase(
                                orderId = orderId,
                                orderItemId = orderItem.id,
                                nearbyStoreId = proposal.detail,
                                pickedQuantity = orderItem.requestedQuantity
                            )
                        }
                        ProposalType.UNAVAILABLE -> {
                            check(currentQuantity < orderItem.requestedQuantity &&
                                currentOptions?.substitutesInCurrentStore.isNullOrEmpty() &&
                                currentOptions?.originalProductNearbyStores.isNullOrEmpty()) {
                                "Fulfilment options changed for " + orderItem.product.name + ". Run AI Pickup again."
                            }
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

                    // Alert the manager when this action leaves the current shelf empty
                    // or below the existing low-stock threshold. For a picked item,
                    // account for the quantity just removed; for a substitute/transfer,
                    // the original item remains at its current-store quantity.
                    if (proposal.proposal != ProposalType.UNAVAILABLE) {
                        val remainingLocalQuantity = if (proposal.proposal == ProposalType.PICKED) {
                            currentQuantity - orderItem.requestedQuantity
                        } else {
                            currentQuantity
                        }
                        if (remainingLocalQuantity < MIN_QUANTITY_TO_INDICATE_LOW) {
                            markItemStockLowUseCase(orderId, orderItem.id, notifyManager = true)
                            notifyStoreManagerUseCase(
                                AssociateNotification(
                                    notificationReasonType = NotificationReasonType.INVENTORY_ISSUE_LOW_STOCK,
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
                // Mirror the manual Order Details status transition after every item update.
                val updatedOrder = orderRepository.observeOrder(orderId).first()
                    ?: throw IllegalStateException("Order not found after applying pickup proposals")
                val updatedStatuses = updatedOrder.items.map { it.pickupStatus }
                val orderStatus = when {
                    updatedStatuses.any { it == ItemPickupStatus.UNAVAILABLE } -> OrderStatus.INCOMPLETE
                    updatedStatuses.all {
                        it == ItemPickupStatus.PICKED ||
                            it == ItemPickupStatus.SUBSTITUTE ||
                            it == ItemPickupStatus.RESERVED_NEARBY_STORE
                    } -> OrderStatus.READY
                    updatedStatuses.any {
                        it == ItemPickupStatus.PICKED || it == ItemPickupStatus.PENDING
                    } -> OrderStatus.PICKING
                    else -> updatedOrder.status
                }
                if (orderStatus != updatedOrder.status) {
                    orderRepository.updateOrderStatus(orderId, orderStatus)
                }

                _uiState.value = currentState.copy(isUpdating = false, isApprovedAndOrderUpdated = true)
            } catch (e: Exception) {
                _uiState.value = AIPickUpUiState.Error(
                    message = "Failed to update order: " + (e.message ?: "Unknown error"),
                    orderId = orderId
                )
            }
        }
    }
}
