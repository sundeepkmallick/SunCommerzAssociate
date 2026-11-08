package com.suncommerz.associate.ai

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.annotations.Tool
import com.google.adk.kt.models.Model
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.repository.StoreRepository
import com.suncommerz.associate.domain.usecase.product.ObserveStoreProductsUseCase
import com.suncommerz.associate.domain.usecase.product.GetSubstituteProductsUseCase
import com.suncommerz.associate.domain.usecase.inventory.IsProductAvailableUseCase
import com.suncommerz.associate.domain.usecase.store.FindNearbyStoresWithProductUseCase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PickupAdkAgentFactory @Inject constructor(
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase,
    private val observeStoreProductsUseCase: ObserveStoreProductsUseCase,
    private val getSubstituteProductsUseCase: GetSubstituteProductsUseCase,
    private val isProductAvailableUseCase: IsProductAvailableUseCase
) {
    inner class InventoryTools {
        @Tool
        suspend fun getProductStockInStore(productId: String, storeId: String): String {
            val inventory = inventoryRepository
                .observeProductInventory(storeId, productId)
                .first()
            return Json.encodeToString(inventory)
        }

        @Tool
        suspend fun getStoreInventory(storeId: String): String {
            val storeInventory = inventoryRepository.observeInventory(storeId).first()
            return Json.encodeToString(storeInventory)
        }
    }

    inner class StoreTools {
        @Tool
        suspend fun findNearbyStores(
            productId: String,
            currentStoreId: String,
            requestedQuantity: Int,
            radiusMeters: Double
        ): String {
            require(requestedQuantity > 0) { "Requested quantity must be greater than zero" }
            require(radiusMeters > 0) { "Search radius must be greater than zero" }

            val currentStore = storeRepository.getStore(currentStoreId).first()
                ?: return "[]"

            val nearbyStores = findNearbyStoresWithProductUseCase.invoke(
                productId = productId,
                currentStore = currentStore,
                quantity = requestedQuantity,
                radiusMeters = radiusMeters
            )
            return Json.encodeToString(nearbyStores)
        }
    }

    inner class SubstitutionTools {
        @Tool
        suspend fun getAvailableSubstitutes(
            productId: String,
            storeId: String,
            requestedQuantity: Int
        ): String {
            require(requestedQuantity > 0) { "Requested quantity must be greater than zero" }

            val candidates = getSubstituteProductsUseCase(productId).first()
                .orEmpty()
                .filterNotNull()
                .filter { candidate ->
                    isProductAvailableUseCase(
                        productId = candidate.id,
                        storeId = storeId,
                        quantity = requestedQuantity
                    )
                }

            return Json.encodeToString(candidates)
        }

        @Tool
        suspend fun observeStoreProductsUseCase(storeId: String): String {
            val products = observeStoreProductsUseCase.invoke(storeId).first()
            return Json.encodeToString(products)
        }

        @Tool
        suspend fun pastOrderWithSubstitutionHistory(productId: String): String {
            val history = orderRepository.pastOrderWithSubstitutionHistory(productId).first()
            return Json.encodeToString(history)
        }
    }

    /** Creates a fresh ADK agent with shared pickup instructions and read-only tools. */
    fun createPickupAgent(model: Model): LlmAgent = LlmAgent(
        name = "PickupOrchestrator",
        description = "Coordinates product pickup for customer orders.",
        model = model,
        instruction = Instruction(PickupAgentInstructions.SYSTEM),
        tools = listOf(
            InventoryTools().generatedTools(),
            StoreTools().generatedTools(),
            SubstitutionTools().generatedTools()
        ).flatten()
    )

}
