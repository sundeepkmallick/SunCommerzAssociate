package com.suncommerz.associate.domain.model

data class OrderItemAvailabilityInStoreUiModel(
    val product: Product,
    val availableQuantity: Int,
    val orderItemAvailabilityStatus: OrderItemAvailabilityStatus
)