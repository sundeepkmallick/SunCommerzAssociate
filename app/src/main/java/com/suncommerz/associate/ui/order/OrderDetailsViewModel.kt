package com.suncommerz.associate.ui.order

import android.adservices.adid.AdId
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.OrderUiModel
import com.suncommerz.associate.domain.usecase.order.ObserveOrderUseCase
import com.suncommerz.associate.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeOrderUseCase: ObserveOrderUseCase
): ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState: MutableStateFlow<OrderDetailsUiState> = MutableStateFlow(OrderDetailsUiState.Loading)
    val uiStat: StateFlow<OrderDetailsUiState> = _uiState

    init {
        getOrderDetails(orderId)
    }

    private fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            observeOrderUseCase.invoke(orderId).map{ order ->
                if(order != null){
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


    fun onOrderItemSelected(orderId: String, orderItemId: String) {
        TODO("Not yet implemented")
    }

}