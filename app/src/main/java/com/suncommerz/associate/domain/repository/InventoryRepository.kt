package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.domain.model.InventoryRecord
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun observeInventory(
        storeId: String
    ): Flow<List<InventoryRecordDto>>

    fun observeProductInventory(
        storeId: String,
        productId: String
    ): Flow<InventoryRecordDto?>

    suspend fun updateStock(
        storeId: String,
        productId: String,
        quantity: Int
    )
}