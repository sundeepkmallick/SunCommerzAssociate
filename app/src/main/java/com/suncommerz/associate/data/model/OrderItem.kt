package com.suncommerz.associate.data.model

data class OrderItem(
    val id: String,
    val product: Product,
    val requestedQuantity: Int,
    val pickupStatus: ItemPickupStatus = ItemPickupStatus.PENDING,
    val pickedQuantity: Int = 0,
    val selectedSubstituteId: String? = null,
    val reservedStoreId: String? = null,
    val managerNotified: Boolean = false,
    val customerNotified: Boolean = false
)
