package com.suncommerz.associate.domain.usecase.inventory

import com.suncommerz.associate.domain.repository.InventoryRepository
import javax.inject.Inject

class GetProductAvailabilityUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        productId: String,
        storeId: String
    ): Int {
        return inventoryRepository.getAvailableQuantity(
            productId = productId,
            storeId = storeId
        )
    }
}