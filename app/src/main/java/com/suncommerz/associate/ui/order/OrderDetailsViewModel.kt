package com.suncommerz.associate.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.OrderUiModel
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.usecase.order.ObserveOrderUseCase
import com.suncommerz.associate.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeOrderUseCase: ObserveOrderUseCase,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState: MutableStateFlow<OrderDetailsUiState> =
        MutableStateFlow(OrderDetailsUiState.Loading)
    val uiState: StateFlow<OrderDetailsUiState> = _uiState

    init {
        getOrderDetails(orderId)
    }

    private fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            observeOrderUseCase.invoke(orderId).map { order ->
                if (order != null) {
                    val orderUiModel = OrderUiModel(
                        order = order,
                        orderDateTimeFormatted = DateTimeUtils.formatInstantToDateTime(order.orderDateTime)
                    )
                    OrderDetailsUiState.Loaded(
                        orderUiModel = orderUiModel
                    )
                } else {
                    OrderDetailsUiState.Error("Unable to fetch Order details!")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateOrderStatus() {
        viewModelScope.launch {
            observeOrderUseCase.invoke(orderId).map { order ->
                val currentState = _uiState.value
                if (currentState is OrderDetailsUiState.Loaded) {
                    _uiState.value = currentState.copy(isUpdateOrderStatusInProgress = true)
                }
                delay(1_000.milliseconds) //artificial delay
                if (order != null) {
                    val items = order.items
                    val status = when {
                        items.any { it.pickupStatus == ItemPickupStatus.UNAVAILABLE } -> {
                            OrderStatus.INCOMPLETE
                        }

                        items.all { it.pickupStatus == ItemPickupStatus.PENDING } -> {
                            OrderStatus.PENDING
                        }

                        items.all {
                            it.pickupStatus == ItemPickupStatus.PICKED ||
                                    it.pickupStatus == ItemPickupStatus.SUBSTITUTE ||
                                    it.pickupStatus == ItemPickupStatus.RESERVED_NEARBY_STORE
                        } -> {
                            OrderStatus.READY
                        }

                        items.any {
                            it.pickupStatus == ItemPickupStatus.PICKED ||
                                    it.pickupStatus == ItemPickupStatus.PENDING
                        } -> {
                            OrderStatus.PICKING
                        }

                        else -> order.status
                    }

                    if (status != order.status) {
                        orderRepository.updateOrderStatus(orderId, status)
                    }
                }
                val finalState = _uiState.value
                if (finalState is OrderDetailsUiState.Loaded) {
                    _uiState.value = finalState.copy(isUpdateOrderStatusInProgress = false)
                }
            }.collect {}
        }
    }

}