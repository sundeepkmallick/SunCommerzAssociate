package com.suncommerz.associate.domain.usecase.inventory

import com.suncommerz.associate.domain.repository.InventoryRepository
import javax.inject.Inject

class IsProductAvailableUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        productId: String,
        storeId: String,
        quantity: Int
    ): Boolean {

        require(quantity >= 0) {
            "Quantity cannot be negative"
        }

        val availableQuantity =
            inventoryRepository.getAvailableQuantity(
                productId = productId,
                storeId = storeId
            )

        return availableQuantity >= quantity
    }
}