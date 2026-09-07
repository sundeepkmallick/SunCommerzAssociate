package com.suncommerz.associate.domain.usecase.fullfilment

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.usecase.inventory.GetProductAvailabilityUseCase
import com.suncommerz.associate.domain.usecase.inventory.IsProductAvailableUseCase
import com.suncommerz.associate.domain.usecase.product.GetSubstituteProductsUseCase
import com.suncommerz.associate.domain.usecase.store.FindNearbyStoresWithProductUseCase
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FindFulfillmentOptionsUseCase @Inject constructor(
    private val getSubstituteProductsUseCase: GetSubstituteProductsUseCase,
    private val isProductAvailableUseCase: IsProductAvailableUseCase,
    private val findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase
) {

    suspend operator fun invoke(
        product: Product,
        currentStore: Store,
        quantity: Int,
        radiusMeters: Double
    ): FulfillmentOptions {

        require(quantity > 0) {
            "Quantity must be greater than zero"
        }

        require(radiusMeters >= 0) {
            "Radius cannot be negative"
        }

        val availableSubstitutesInCurrentStore =
            getSubstituteProductsUseCase(productId = product.id).map { substituteList ->
                substituteList?.filterNotNull()?.filter { substituteProduct ->
                    isProductAvailableUseCase(
                        productId = substituteProduct.id,
                        storeId = currentStore.id,
                        quantity = quantity
                    )
                }
            }.first()

        val nearbyStoresWithOriginalProduct = findNearbyStoresWithProductUseCase(
            productId = product.id,
            currentStore = currentStore,
            quantity = quantity,
            radiusMeters = radiusMeters
        )

        return FulfillmentOptions(
            substitutesInCurrentStore = availableSubstitutesInCurrentStore,
            originalProductNearbyStores = nearbyStoresWithOriginalProduct,
        )
    }
}