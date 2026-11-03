package com.suncommerz.associate.ai

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.annotations.Tool
import com.suncommerz.associate.ai.provider.FirebaseModelProvider
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
class AIAgentManager @Inject constructor(
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val firebaseModelProvider: FirebaseModelProvider,
    private val storeRepository: StoreRepository,
    private val findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase,
    private val observeStoreProductsUseCase: ObserveStoreProductsUseCase,
    private val getSubstituteProductsUseCase: GetSubstituteProductsUseCase,
    private val isProductAvailableUseCase: IsProductAvailableUseCase
) {
    private var rootOrchestratorAgent: LlmAgent? = null

    inner class InventoryTools {

        @Tool
        suspend fun getProductStockInStore(
            productId: String,
            storeId: String
        ): String {
            println("AI_TOOL: getProductStockInStore START")

            val inventory = inventoryRepository
                .observeProductInventory(storeId, productId)
                .first()
            val result = Json.encodeToString(inventory)

            println("AI_TOOL: getProductStockInStore END: $result")

            return result
        }

        @Tool
        suspend fun getStoreInventory(
            storeId: String
        ): String {
            println("AI_TOOL: getStoreInventory START")

            val storeInventory = inventoryRepository.observeInventory(storeId).first()
            val result = Json.encodeToString(storeInventory)

            println("AI_TOOL: getStoreInventory END: $result")

            return result
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
            println("AI_TOOL: findNearbyStores START")

            val currentStore = storeRepository
                .getStore(currentStoreId)
                .first()
                ?: return "[]"

            val nearbyStores = findNearbyStoresWithProductUseCase.invoke(
                productId = productId,
                currentStore = currentStore,
                quantity = requestedQuantity,
                radiusMeters = radiusMeters
            )

            println("AI_TOOL: findNearbyStores END")

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
        suspend fun observeStoreProductsUseCase(
            storeId: String
        ): String {
            println("AI_TOOL: observeStoreProductsUseCase START")

            val products = observeStoreProductsUseCase.invoke(storeId).first()
            val result = Json.encodeToString(products)
            println("AI_TOOL: observeStoreProductsUseCase END")

            return result
        }

        @Tool
        suspend fun pastOrderWithSubstitutionHistory(productId: String): String {
            println("AI_TOOL: pastOrderWithSubstitutionHistory START")

            val substitutionHistory = orderRepository.pastOrderWithSubstitutionHistory(productId).first()
            val result = Json.encodeToString(substitutionHistory)
            println("AI_TOOL: pastOrderWithSubstitutionHistory END")

            return result
        }
    }

    suspend fun getRootOrchestratorAgent(): LlmAgent {
        rootOrchestratorAgent?.let { agent ->
            return agent
        }

        val model = firebaseModelProvider.getModel()
        val agent = LlmAgent(
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

        rootOrchestratorAgent = agent
        return agent
    }
}
