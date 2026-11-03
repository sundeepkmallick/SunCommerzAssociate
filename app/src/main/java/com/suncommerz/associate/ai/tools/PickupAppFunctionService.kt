package com.suncommerz.associate.ai.tools

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionAccessLevel
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.ExperimentalAppFunctionsApi
import androidx.appfunctions.metadata.AppFunctionMetadata
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.repository.StoreRepository
import com.suncommerz.associate.domain.usecase.inventory.IsProductAvailableUseCase
import com.suncommerz.associate.domain.usecase.product.GetSubstituteProductsUseCase
import com.suncommerz.associate.domain.usecase.product.ObserveStoreProductsUseCase
import com.suncommerz.associate.domain.usecase.store.FindNearbyStoresWithProductUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * AppFunctions exposed only to this application for Gemini Nano's pickup agent.
 *
 * AppFunctions are only available on Android 16+; older devices use the Firebase ADK agent.
 */
@RequiresApi(36)
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(
    serviceName = "PickupAppFunctionService",
    appFunctionXmlFileName = "pickup_app_functions",
)
abstract class PickupAppFunctionService : AppFunctionService() {

    @Inject lateinit var inventoryRepository: InventoryRepository
    @Inject lateinit var orderRepository: OrderRepository
    @Inject lateinit var storeRepository: StoreRepository
    @Inject lateinit var findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase
    @Inject lateinit var observeStoreProductsUseCaseDependency: ObserveStoreProductsUseCase
    @Inject lateinit var getSubstituteProductsUseCase: GetSubstituteProductsUseCase
    @Inject lateinit var isProductAvailableUseCase: IsProductAvailableUseCase

    /**
     * Gets the available quantity of a product at a store.
     *
     * @param productId The product identifier to inspect.
     * @param storeId The store identifier where stock is checked.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun getProductStockInStore(productId: String, storeId: String): String =
        withContext(Dispatchers.IO) {
            val inventory = inventoryRepository
                .observeProductInventory(storeId, productId)
                .first()
            Json.encodeToString(inventory)
        }

    /**
     * Gets the complete inventory snapshot for a store.
     *
     * @param storeId The store identifier to inspect.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun getStoreInventory(storeId: String): String = withContext(Dispatchers.IO) {
        Json.encodeToString(inventoryRepository.observeInventory(storeId).first())
    }

    /**
     * Finds nearby stores that may carry the requested product.
     *
     * @param productId The product identifier to search for.
     * @param currentStoreId The current store identifier.
     * @param requestedQuantity The quantity required by the order.
     * @param radiusMeters The maximum search radius in meters.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun findNearbyStores(
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ): String = withContext(Dispatchers.IO) {
        require(requestedQuantity > 0) { "Requested quantity must be greater than zero" }
        require(radiusMeters > 0) { "Search radius must be greater than zero" }

        val currentStore = storeRepository.getStore(currentStoreId).first()
            ?: return@withContext "[]"

        val nearbyStores = findNearbyStoresWithProductUseCase.invoke(
            productId = productId,
            currentStore = currentStore,
            quantity = requestedQuantity,
            radiusMeters = radiusMeters,
        )
        Json.encodeToString(nearbyStores)
    }

    /**
     * Returns ranked substitute products that have sufficient stock at the current store.
     *
     * @param productId The unavailable product identifier.
     * @param storeId The current store identifier.
     * @param requestedQuantity The quantity required by the order.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun getAvailableSubstitutes(
        productId: String,
        storeId: String,
        requestedQuantity: Int,
    ): String = withContext(Dispatchers.IO) {
        require(requestedQuantity > 0) { "Requested quantity must be greater than zero" }

        val candidates = getSubstituteProductsUseCase(productId).first()
            .orEmpty()
            .filterNotNull()
            .filter { candidate ->
                isProductAvailableUseCase(
                    productId = candidate.id,
                    storeId = storeId,
                    quantity = requestedQuantity,
                )
            }
        Json.encodeToString(candidates)
    }

    /**
     * Gets the products currently listed by a store.
     *
     * @param storeId The store identifier whose products are requested.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun observeStoreProductsUseCase(storeId: String): String =
        withContext(Dispatchers.IO) {
            Json.encodeToString(observeStoreProductsUseCaseDependency(storeId).first())
        }

    /**
     * Gets previous substitution history for a product.
     *
     * @param productId The product identifier whose substitution history is requested.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    @AppFunctionAccessLevel(
        level = AppFunctionMetadata.ACCESS_LEVEL_SELF,
        isCompatEnforcementEnabled = true,
    )
    suspend fun pastOrderWithSubstitutionHistory(productId: String): String =
        withContext(Dispatchers.IO) {
            Json.encodeToString(orderRepository.pastOrderWithSubstitutionHistory(productId).first())
        }
}
