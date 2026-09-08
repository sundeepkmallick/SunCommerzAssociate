package com.suncommerz.associate.ui.order

import com.suncommerz.associate.domain.model.OrderUiModel

sealed class OrderDetailsUiState {
    data object Loading : OrderDetailsUiState()
    data class Error(val message: String): OrderDetailsUiState()
    data class Loaded(
        val orderUiModel: OrderUiModel,
        val selectedOrderItemId: String? = null,
        val isUpdateOrderStatusInProgress: Boolean = false
    ): OrderDetailsUiState()
}