package com.suncommerz.associate.data.dto

data class OrderItemDto(
    val id: String,
    val productDto: ProductDto,
    val requestedQuantity: Int,
    val pickupStatus: ItemPickupStatusDto = ItemPickupStatusDto.PENDING,
    val pickedQuantity: Int = 0,
    val selectedSubstituteId: String? = null,
    val reservedStoreId: String? = null,
    val managerNotified: Boolean = false,
    val customerNotified: Boolean = false
)
