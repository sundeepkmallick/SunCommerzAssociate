package com.suncommerz.associate.domain.usecase.product

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveStoreProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository
) {
    operator fun invoke(storeId: String): Flow<List<Product>> {
        return combine(
            productRepository.observeProducts(),
            inventoryRepository.observeInventory(storeId)
        ) { products, inventory ->

            val productIds = inventory
                .map { it.productId }
                .toSet()

            products.filter { it.id in productIds }
        }
    }
}
