package com.suncommerz.associate.domain.model

data class Reservation(
    val id: String,
    val orderId: String,
    val itemId: String,
    val storeId: String,
    val status: ItemPickupStatus = ItemPickupStatus.RESERVED_NEARBY_STORE
)
