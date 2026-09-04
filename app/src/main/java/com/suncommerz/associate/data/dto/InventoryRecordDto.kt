package com.suncommerz.associate.data.dto

data class InventoryRecordDto(
    val productId: String,
    val storeId: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5
)
