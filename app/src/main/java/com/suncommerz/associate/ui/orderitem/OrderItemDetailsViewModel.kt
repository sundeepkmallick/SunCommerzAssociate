package com.suncommerz.associate.ui.orderitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.model.NotificationReasonType
import com.suncommerz.associate.domain.model.OrderItemAvailabilityInStoreUiModel
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.usecase.fullfilment.FindFulfillmentOptionsUseCase
import com.suncommerz.associate.domain.usecase.fullfilment.FulfillmentOptions
import com.suncommerz.associate.domain.usecase.inventory.GetProductAvailabilityUseCase
import com.suncommerz.associate.domain.usecase.notification.NotifyStoreManagerUseCase
import com.suncommerz.associate.domain.usecase.order.ObserveOrderUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemPickedUseCase
import com.suncommerz.associate.domain.usecase.orderitem.MarkItemUnavailableUseCase
import com.suncommerz.associate.domain.usecase.orderitem.ObserveOrderItemUseCase
import com.suncommerz.associate.domain.usecase.orderitem.ReserveNearbyStoreUseCase
import com.suncommerz.associate.domain.usecase.orderitem.SelectSubstituteUseCase
import com.suncommerz.associate.domain.usecase.storemanager.ObserveStoreManagerUseCase
import com.suncommerz.associate.domain.usecase.user.ObserveUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

const val NEARBY_STORE_RADIUS = 3.00
const val MIN_QUANTITY_TO_INDICATE_LOW = 2


@HiltViewModel
class OrderItemDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeOrderUseCase: ObserveOrderUseCase,
    private val observeOrderItemUseCase: ObserveOrderItemUseCase,
    private val getProductAvailabilityUseCase: GetProductAvailabilityUseCase,
    private val findFulfillmentOptionsUseCase: FindFulfillmentOptionsUseCase,
    private val selectSubstituteUseCase: SelectSubstituteUseCase,
    private val reserveNearbyStoreUseCase: ReserveNearbyStoreUseCase,
    private val markItemPickedUseCase: MarkItemPickedUseCase,
    private val markItemUnavailableUseCase: MarkItemUnavailableUseCase,
    private val notifyStoreManagerUseCase: NotifyStoreManagerUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeStoreManagerUseCase: ObserveStoreManagerUseCase
) : ViewModel() {
    private val assignedAssociateAndStoreManager =
        MutableStateFlow<Pair<StoreAssociate?, StoreManager?>?>(null)
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val orderItemId: String = checkNotNull(savedStateHandle["orderItemId"])

    private val _uiState: MutableStateFlow<OrderItemDetailsUiState> =
        MutableStateFlow(OrderItemDetailsUiState.Loading)
    val uiState: StateFlow<OrderItemDetailsUiState> = _uiState

    private val _events = Channel<OrderItemDetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()


    init {
        getAssignedAssociateAndStoreManager()
        getOrderItemDetails(orderId, orderItemId)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getOrderItemDetails(orderId: String, orderItemId: String) {
        viewModelScope.launch {
            observeOrderUseCase.invoke(orderId).flatMapLatest { order ->
                if (order == null) {
                    flowOf(OrderItemDetailsUiState.Error("Error while fetching Order!"))
                } else {
                    observeOrderItemUseCase.observeOrderItem(
                        orderId = orderId,
                        orderItemId = orderItemId
                    ).map { orderItem ->
                        orderItem?.let { item ->
                            val availableQuantity = getProductAvailabilityUseCase.invoke(
                                productId = item.product.id,
                                storeId = order.store.id,
                            )

                            val isEnoughAvailable = availableQuantity >= item.requestedQuantity

                            val fulfillmentOptions = if (!isEnoughAvailable) {
                                findFulfillmentOptionsUseCase.invoke(
                                    product = item.product,
                                    currentStore = order.store,
                                    quantity = item.requestedQuantity,
                                    radiusMeters = NEARBY_STORE_RADIUS
                                )
                            } else null

                            val orderItemAvailabilityInStoreUiModel =
                                prepareOrderItemAvailabilityInStoreUiModel(
                                    item.product,
                                    availableQuantity,
                                    item.requestedQuantity,
                                    fulfillmentOptions
                                )

                            OrderItemDetailsUiState.Loaded(
                                orderItem = item,
                                orderItemAvailabilityInStoreUiModel = orderItemAvailabilityInStoreUiModel,
                                fulfillmentOptions = fulfillmentOptions
                            )

                        } ?: OrderItemDetailsUiState.Error("Error while fetching Order Item!")
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun prepareOrderItemAvailabilityInStoreUiModel(
        product: Product,
        availableQuantity: Int,
        requestedQuantity: Int,
        fulfillmentOptions: FulfillmentOptions?
    ): OrderItemAvailabilityInStoreUiModel {
        val status = when {
            availableQuantity >= requestedQuantity -> OrderItemAvailabilityStatus.AVAILABLE
            fulfillmentOptions?.substitutesInCurrentStore?.isNotEmpty() == true -> OrderItemAvailabilityStatus.SUBSTITUTE_AVAILABLE
            fulfillmentOptions?.originalProductNearbyStores?.isNotEmpty() == true -> OrderItemAvailabilityStatus.AVAILABLE_IN_NEARBY_STORE
            else -> OrderItemAvailabilityStatus.OUT_OF_STOCK
        }

        val isStockLow = availableQuantity in 1..<MIN_QUANTITY_TO_INDICATE_LOW

        return OrderItemAvailabilityInStoreUiModel(
            product = product,
            availableQuantity = availableQuantity,
            isStockLow = isStockLow,
            orderItemAvailabilityStatus = status
        )
    }

    fun onSubstituteProductItemSelected(substituteProductId: String, requestedQuantity: Int) {
        viewModelScope.launch {
            selectSubstituteUseCase.invoke(
                orderId = orderId,
                orderItemId = orderItemId,
                substituteProductId = substituteProductId,
                pickedQuantity = requestedQuantity,
            )
        }
    }

    fun orderItemInNearByStoreReserved(nearByStoreId: String, requestedQuantity: Int) {
        viewModelScope.launch {
            reserveNearbyStoreUseCase.invoke(
                orderId = orderId,
                orderItemId = orderItemId,
                nearbyStoreId = nearByStoreId,
                pickedQuantity = requestedQuantity
            )
        }
    }

    fun notifyOutOfStock(orderId: String, orderItemId: String, productId: String) {
        viewModelScope.launch {
            markItemUnavailableUseCase.invoke(
                orderId = orderId,
                orderItemId = orderItemId,
                notifyManager = true,
                notifyCustomer = true
            )
            val (reportedByAssociateId, reportedToManagerId) =
                assignedAssociateAndStoreManager
                    .filterNotNull()
                    .first()

            val notification = AssociateNotification(
                notificationReasonType = NotificationReasonType.INVENTORY_ISSUE_LOW_STOCK,
                orderId = orderId,
                productId = productId,
                reportedByAssociateId = reportedByAssociateId?.id.orEmpty(),
                reportedToManagerId = reportedToManagerId?.id.orEmpty(),
                reportDateTime = Clock.System.now()
            )

            notifyStoreManagerUseCase(notification)

            _events.send(
                OrderItemDetailsEvent.ShowToastNotified(
                    NotificationReasonType.INVENTORY_ISSUE_OUT_OF_STOCK
                )
            )
        }
    }

    fun notifyStockLow(orderId: String, orderItemId: String, productId: String) {
        viewModelScope.launch {

            val (reportedByAssociateId, reportedToManagerId) =
                assignedAssociateAndStoreManager
                    .filterNotNull()
                    .first()

            notifyStoreManagerUseCase(
                AssociateNotification(
                    notificationReasonType = NotificationReasonType.INVENTORY_ISSUE_LOW_STOCK,
                    orderId = orderId,
                    productId = productId,
                    reportedByAssociateId = reportedByAssociateId?.id.orEmpty(),
                    reportedToManagerId = reportedToManagerId?.id.orEmpty(),
                    reportDateTime = Clock.System.now()
                )
            )

            _events.send(
                OrderItemDetailsEvent.ShowToastNotified(
                    NotificationReasonType.INVENTORY_ISSUE_LOW_STOCK
                )
            )
        }
    }

    fun pickUpItem(orderId: String, productId: String, requestedQuantity: Int) {
        viewModelScope.launch {
            markItemPickedUseCase.invoke(
                orderId,
                productId,
                requestedQuantity
            )
        }
    }
}