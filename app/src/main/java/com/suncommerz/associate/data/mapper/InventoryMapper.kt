package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.domain.model.InventoryRecord

fun InventoryRecordDto.toDomain(): InventoryRecord {
    return InventoryRecord(
        productId = productId,
        storeId = storeId,
        stockQuantity = stockQuantity,
        lowStockThreshold = lowStockThreshold
    )
}