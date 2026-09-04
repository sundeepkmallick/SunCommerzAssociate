package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(val api: FakeBackendApiResponse): InventoryRepository {

    override fun observeInventory(storeId: String): Flow<List<InventoryRecordDto>> {
        return api.inventoryRecords.map { inventoryRecords ->
            inventoryRecords.filter {
                it.storeId == storeId
            }
        }
    }

    override fun observeProductInventory(
        storeId: String,
        productId: String
    ): Flow<InventoryRecordDto?> {
        return api.inventoryRecords.map { inventoryRecords ->
            inventoryRecords.find {
                it.storeId == storeId && it.productId == productId
            }
        }
    }

    override suspend fun updateStock(
        storeId: String,
        productId: String,
        quantity: Int
    ) {
        val inventoryUpdate = api.inventoryRecords.value.map {
            if (it.storeId == storeId && it.productId == productId){
                it.copy(
                    stockQuantity = quantity
                )
            } else {
                it
            }
        }

        api.updateInventory(inventoryUpdate)
    }

}