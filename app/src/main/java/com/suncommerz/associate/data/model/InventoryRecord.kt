package com.suncommerz.associate.data.model

data class InventoryRecord(
    val productId: String,
    val storeId: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5
)
