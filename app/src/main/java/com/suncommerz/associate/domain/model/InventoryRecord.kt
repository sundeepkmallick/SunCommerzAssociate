package com.suncommerz.associate.domain.model

data class InventoryRecord(
    val productId: String,
    val storeId: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5
)
