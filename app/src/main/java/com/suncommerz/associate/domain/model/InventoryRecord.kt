package com.suncommerz.associate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryRecord(
    val productId: String,
    val storeId: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5
)
