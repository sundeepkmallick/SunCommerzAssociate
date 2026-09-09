package com.suncommerz.associate.ai

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.annotations.Tool
import com.suncommerz.associate.ai.provider.HybridModelProvider
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.repository.ProductRepository
import com.suncommerz.associate.domain.repository.StoreRepository
import com.suncommerz.associate.domain.usecase.product.ObserveStoreProductsUseCase
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
    private val productRepository: ProductRepository,
    private val modelProvider: HybridModelProvider,
    private val storeRepository: StoreRepository,
    private val findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase,
    private val observeStoreProductsUseCase: ObserveStoreProductsUseCase
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

            println("AI_TOOL: getProductStockInStore END: $result")

            return result
        }
    }

    inner class StoreTools {

        @Tool
        suspend fun findNearbyStores(
            orderId: String,
            productId: String,
            currentStoreId: String,
            requestedQuantity: Int,
            radius: Double
        ): String {
            println("AI_TOOL: findNearbyStores START")

            val currentStore = storeRepository
                .getStore(currentStoreId)
                .first()

            println("AI_TOOL: currentStore loaded")

            val nearbyStores = findNearbyStoresWithProductUseCase.invoke(
                productId = productId,
                currentStore = currentStore!!,
                quantity = requestedQuantity,
                radiusMeters = radius
            )

            println("AI_TOOL: findNearbyStores END")

            return Json.encodeToString(nearbyStores)
        }
    }

    inner class SubstitutionTools {

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
        suspend fun pastOrderWithSubstitutionHistory(productId: String): String{
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

        val model = modelProvider.getModel()

        val agent = LlmAgent(
            name = "PickupOrchestrator",
            description = "Coordinates product pickup for customer orders.",
            model = model,
            instruction = Instruction(
                """
                You are the Pickup Orchestrator for a retail store.

                Your job is to determine how each requested order item
                should be fulfilled.

                For each product, follow this decision process:

                1. Check the product's available stock at the current store.

                2. If the available quantity is greater than or equal
                   to the requested quantity:
                   
                   Return:
                   PICKED
                
                3. If the product is not available then
                   
                   - Search for suitable substitute products.
                   - Prefer substitutes with:
                       * Same category
                       * Similar function/use case
                       * Compatible ingredients
                       * Similar size or quantity
                       * Similar brand/type where appropriate
                       * Similar price
                   
                   Return
                   SUBSTITUTE(productId)
                   
                4. If the current store does not have enough stock and you didn't find
                   substitute product or substitute product is also out of stock then:

                   - Find nearby stores.
                   - Check the product stock at the nearby stores.

                5. If a nearby store has enough stock, then reserve the one that is
                   nearest to the store:

                   Return:
                   RESERVE_NEARBY_STORE(storeId)

                6. If the product cannot be obtained from the current store or nearby stores:

                   Return:
                   UNAVAILABLE

                IMPORTANT:

                - Never recommend the unavailable product itself.
                - Always use the tools to obtain real inventory information.
                - Do not invent product IDs or store IDs.
                - Do not assume a product is available without checking.
                - Do not mark an item PICKED if sufficient stock does not exist.

                Valid actions are:

                PICKED
                RESERVE_NEARBY_STORE(storeId)
                SUBSTITUTE(productId)
                UNAVAILABLE

                Return only the action for each item.
                Do not provide explanations unless specifically requested.
                """.trimIndent()
            ),

            tools = listOf(
                InventoryTools().generatedTools(),
                StoreTools().generatedTools(),
                SubstitutionTools().generatedTools()
            ).flatten()
        )

        rootOrchestratorAgent = agent

        return agent
    }


    fun isUsingLocalModel(): Boolean {
        return modelProvider.isLocalModelAvailable()
    }

    fun getCurrentBackend(): AIBackend {
        return if (modelProvider.isLocalModelAvailable()) {
            AIBackend.LOCAL
        } else {
            AIBackend.FIREBASE
        }
    }
}

enum class AIBackend {
    LOCAL,
    FIREBASE
}

