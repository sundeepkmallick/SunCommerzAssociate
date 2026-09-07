package com.suncommerz.associate.data.dto

data class OrderItemDto(
    val id: String,
    val orderIdDto: String,
    val productDto: ProductDto,
    val quantity: Int,
    val pickupStatus: ItemPickupStatusDto,
    val pickedQuantity: Int = 0,
    val managerNotified: Boolean = false,
    val customerNotified: Boolean = false,
    val reservedStoreId: String? = null,
    val selectedSubstituteId: String? = null,
    val substitutionReason: SubstitutionReason? = null
)

enum class SubstitutionReason {
    OUT_OF_STOCK,
    CUSTOMER_REQUEST,
    ASSOCIATE_RECOMMENDATION
}