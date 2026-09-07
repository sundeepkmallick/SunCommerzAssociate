package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.InventoryRecord
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun observeInventory(
        storeId: String
    ): Flow<List<InventoryRecord>>

    fun observeProductInventory(
        storeId: String,
        productId: String
    ): Flow<InventoryRecord?>

    suspend fun updateStock(
        storeId: String,
        productId: String,
        quantity: Int
    )

    fun getAvailableQuantity(productId: String, storeId: String): Int
}