package com.suncommerz.associate.ui.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.usecase.fullfilment.FulfillmentOptions

sealed class OrderItemDetailsUiState {
    data object Loading: OrderItemDetailsUiState()
    data class Error(val message: String): OrderItemDetailsUiState()
    data class Loaded(
        val orderItem: OrderItem,
        val substituteProducts: List<Product> = emptyList(),
        val fulfillmentOptions: FulfillmentOptions? = null,
        val actionState: ItemPickupStatus = ItemPickupStatus.PENDING
    ): OrderItemDetailsUiState()
}