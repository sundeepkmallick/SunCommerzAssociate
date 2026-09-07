package com.suncommerz.associate.domain.usecase.store

import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.InventoryRepository
import javax.inject.Inject

class FindNearbyStoresWithProductUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val getNearbyStoresUseCase: GetNearbyStoresUseCase
) {
    suspend operator fun invoke(
        productId: String,
        currentStore: Store,
        quantity: Int,
        radiusMeters: Double
    ): List<Store> {

        val nearbyStores = getNearbyStoresUseCase(
            currentStoreLocation = currentStore.location,
            radiusMeters = radiusMeters
        )

        return nearbyStores.filter { store ->

            val availableQuantity =
                inventoryRepository.getAvailableQuantity(
                    productId = productId,
                    storeId = store.id
                )

            availableQuantity >= quantity
        }
    }
}