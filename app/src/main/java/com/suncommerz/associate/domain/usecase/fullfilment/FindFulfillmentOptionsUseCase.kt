package com.suncommerz.associate.domain.usecase.fullfilment

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.usecase.inventory.IsProductAvailableUseCase
import com.suncommerz.associate.domain.usecase.product.GetSubstituteProductsUseCase
import com.suncommerz.associate.domain.usecase.store.FindNearbyStoresWithProductUseCase
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

        val substituteProducts =
            getSubstituteProductsUseCase(
                productId = product.id
            )

        val availableSubstitutesInCurrentStore =
            substituteProducts.filter { substitute ->

                isProductAvailableUseCase(
                    productId = substitute.id,
                    storeId = currentStore.id,
                    quantity = quantity
                )
            }

        val nearbyStoresWithOriginalProduct =
            findNearbyStoresWithProductUseCase(
                productId = product.id,
                currentStore = currentStore,
                quantity = quantity,
                radiusMeters = radiusMeters
            )

        val nearbyStoresWithSubstitutes =
            substituteProducts.associateWith { substitute ->

                findNearbyStoresWithProductUseCase(
                    productId = substitute.id,
                    currentStore = currentStore,
                    quantity = quantity,
                    radiusMeters = radiusMeters
                )
            }

        return FulfillmentOptions(
            substitutesInCurrentStore =
                availableSubstitutesInCurrentStore,

            originalProductNearbyStores =
                nearbyStoresWithOriginalProduct,

            substitutesInNearbyStores =
                nearbyStoresWithSubstitutes
        )
    }
}