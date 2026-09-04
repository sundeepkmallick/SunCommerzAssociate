package com.suncommerz.associate.data.dto

data class ReservationDto(
    val id: String,
    val orderId: String,
    val itemId: String,
    val storeId: String,
    val status: ItemPickupStatusDto = ItemPickupStatusDto.RESERVED_NEARBY_STORE
)
