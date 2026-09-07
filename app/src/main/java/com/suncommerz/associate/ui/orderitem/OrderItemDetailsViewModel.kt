package com.suncommerz.associate.ui.orderitem

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.OrderItemAvailabilityInStoreUiModel
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.usecase.fullfilment.FindFulfillmentOptionsUseCase
import com.suncommerz.associate.domain.usecase.fullfilment.FulfillmentOptions
import com.suncommerz.associate.domain.usecase.inventory.GetProductAvailabilityUseCase
import com.suncommerz.associate.domain.usecase.order.ObserveOrderUseCase
import com.suncommerz.associate.domain.usecase.orderitem.ObserveOrderItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NEARBY_STORE_RADIUS = 3.00
const val MIN_QUANTITY_TO_INDICATE_LOW = 2


@HiltViewModel
class OrderItemDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeOrderUseCase: ObserveOrderUseCase,
    private val observeOrderItemUseCase: ObserveOrderItemUseCase,
    private val getProductAvailabilityUseCase: GetProductAvailabilityUseCase,
    private val findFulfillmentOptionsUseCase: FindFulfillmentOptionsUseCase
) : ViewModel() {
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val orderItemId: String = checkNotNull(savedStateHandle["orderItemId"])

    private val _uiState: MutableStateFlow<OrderItemDetailsUiState> =
        MutableStateFlow(OrderItemDetailsUiState.Loading)
    val uiState: StateFlow<OrderItemDetailsUiState> = _uiState

    init {
        getOrderItemDetails(orderId, orderItemId)
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
            availableQuantity > 0 && availableQuantity < MIN_QUANTITY_TO_INDICATE_LOW -> OrderItemAvailabilityStatus.LOW
            else -> OrderItemAvailabilityStatus.OUT_OF_STOCK
        }

        return OrderItemAvailabilityInStoreUiModel(
            product = product,
            availableQuantity = availableQuantity,
            orderItemAvailabilityStatus = status
        )
    }
}