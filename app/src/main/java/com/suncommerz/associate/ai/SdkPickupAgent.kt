package com.suncommerz.associate.ai

import com.sunmobile.aiagent.adk.AdkAgentBackendProvider
import com.sunmobile.aiagent.core.Agent
import com.sunmobile.aiagent.core.AgentBuilder
import com.sunmobile.aiagent.core.AgentConfiguration
import com.sunmobile.aiagent.core.AgentPolicy
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.core.AgentTool
import com.sunmobile.aiagent.core.ToolDefinition
import com.sunmobile.aiagent.core.ToolExecutionContext
import com.sunmobile.aiagent.core.ToolRegistry
import com.sunmobile.aiagent.core.ToolResult
import com.sunmobile.aiagent.core.ToolRiskLevel
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.StoreRepository
import com.suncommerz.associate.domain.usecase.inventory.IsProductAvailableUseCase
import com.suncommerz.associate.domain.usecase.product.GetSubstituteProductsUseCase
import com.suncommerz.associate.domain.usecase.store.FindNearbyStoresWithProductUseCase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.put
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Host-owned BOPIS tool composition using the reusable SDK's ADK backend, registry,
 * schema validation and policy executor. The order/store/product scope is captured
 * from the authenticated host workflow; model-supplied identifiers cannot change it.
 */
class SdkPickupAgent @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val storeRepository: StoreRepository,
    private val findNearbyStoresWithProductUseCase: FindNearbyStoresWithProductUseCase,
    private val getSubstituteProductsUseCase: GetSubstituteProductsUseCase,
    private val isProductAvailableUseCase: IsProductAvailableUseCase,
) {
    suspend fun run(
        modelFactory: com.sunmobile.aiagent.adk.AdkModelFactory,
        prompt: String,
        userId: String,
        sessionId: String,
        productId: String,
        currentStoreId: String,
        requestedQuantity: Int,
        radiusMeters: Double,
    ): String {
        require(productId.isNotBlank() && currentStoreId.isNotBlank())
        require(requestedQuantity > 0 && radiusMeters > 0.0)

        val allowedStoreIds = ConcurrentHashMap.newKeySet<String>().apply { add(currentStoreId) }
        val registry = ToolRegistry()
        registry.register(tool(
            name = "getProductStockInStore",
            description = "Read current stock for the requested product at the current store or a nearby store previously returned by findNearbyStores.",
            parameters = objectSchema("productId" to "string", "storeId" to "string"),
        ) { args, _ ->
            val requestedProduct = args.string("productId")
            val requestedStore = args.string("storeId")
            require(requestedProduct == productId) { "Only the current order product can be checked" }
            require(requestedStore in allowedStoreIds) { "Store was not authorized by the current pickup workflow" }
            val quantity = inventoryRepository.getAvailableQuantity(productId, requestedStore)
            buildJsonObject {
                put("productId", productId)
                put("storeId", requestedStore)
                put("availableQuantity", quantity)
            }.toString()
        })
        registry.register(tool(
            name = "getAvailableSubstitutes",
            description = "Find substitute products that have sufficient current stock in the current store.",
            parameters = objectSchema("productId" to "string", "storeId" to "string", "requestedQuantity" to "integer"),
        ) { args, _ ->
            require(args.string("productId") == productId) { "Only the current order product can be substituted" }
            require(args.string("storeId") == currentStoreId) { "Substitutes can only be checked in the current store" }
            require(args.int("requestedQuantity") == requestedQuantity) { "Requested quantity is fixed by the order" }
            val candidates = getSubstituteProductsUseCase(productId).first().orEmpty().filterNotNull()
                .filter { candidate ->
                    isProductAvailableUseCase(candidate.id, currentStoreId, requestedQuantity)
                }
            Json.encodeToString(candidates)
        })
        registry.register(tool(
            name = "findNearbyStores",
            description = "Find nearby stores with enough stock of the current order product and requested quantity.",
            parameters = objectSchema(
                "productId" to "string", "currentStoreId" to "string",
                "requestedQuantity" to "integer", "radiusMeters" to "number",
            ),
        ) { args, _ ->
            require(args.string("productId") == productId) { "Only the current order product can be searched" }
            require(args.string("currentStoreId") == currentStoreId) { "Current store is fixed by the order" }
            require(args.int("requestedQuantity") == requestedQuantity) { "Requested quantity is fixed by the order" }
            require(args.double("radiusMeters") > 0.0 && args.double("radiusMeters") <= radiusMeters) { "Search radius must be positive and cannot exceed the host workflow limit" }
            val currentStore = storeRepository.getStore(currentStoreId).first()
                ?: error("Current store no longer exists")
            val stores = findNearbyStoresWithProductUseCase(
                productId = productId,
                currentStore = currentStore,
                quantity = requestedQuantity,
                radiusMeters = args.double("radiusMeters"),
            )
            allowedStoreIds.addAll(stores.map { it.id })
            Json.encodeToString(stores)
        })

        val agent = AgentBuilder()
            .configuration(AgentConfiguration(
                name = "PickupOrchestrator",
                instructions = PickupAgentInstructions.SYSTEM.replace(
                    "You may use getStoreInventory, observeStoreProductsUseCase, and\n" +
                        "pastOrderWithSubstitutionHistory when their data is useful, but never replace a direct\n" +
                        "stock check with assumptions.",
                    "",
                ),
                maxTurns = 10,
            ))
            .backend(AdkAgentBackendProvider(modelFactory, "SunCommerzAssociatePickup"))
            .tools(registry)
            .policy(AgentPolicy { _, _, context ->
                if (context.toolRiskLevel == ToolRiskLevel.READ_ONLY) {
                    com.sunmobile.aiagent.core.ToolAuthorization.Allow
                } else {
                    com.sunmobile.aiagent.core.ToolAuthorization.Deny("Pickup proposal tools are read-only")
                }
            })
            .build()

        return try {
            val result = agent.execute(AgentRequest(
                prompt = prompt,
                sessionId = sessionId,
                metadata = mapOf("userId" to userId),
            ))
            check(result.toolInvocations.any { invocation ->
                invocation.toolName.substringAfterLast('.')
                    .equals("getProductStockInStore", ignoreCase = true) &&
                    (invocation.arguments as? JsonObject)?.let { args ->
                        args["productId"]?.jsonPrimitive?.content == productId &&
                            args["storeId"]?.jsonPrimitive?.content == currentStoreId
                    } == true
            }) {
                "SDK pickup agent did not verify current-store stock with getProductStockInStore"
            }
            check(!result.text.isNullOrBlank()) { "SDK pickup agent completed without an assistant response" }
            result.text!!.trim()
        } finally {
            agent.close()
        }
    }

    private fun tool(
        name: String,
        description: String,
        parameters: JsonObject,
        action: suspend (JsonObject, ToolExecutionContext) -> String,
    ): AgentTool = object : AgentTool {
        override val definition = ToolDefinition(
            name = name,
            description = description,
            parameters = parameters,
            riskLevel = ToolRiskLevel.READ_ONLY,
        )
        override suspend fun execute(arguments: JsonObject, context: ToolExecutionContext): ToolResult =
            ToolResult(action(arguments, context))
    }

    private fun objectSchema(vararg properties: Pair<String, String>): JsonObject = buildJsonObject {
        put("type", "object")
        put("additionalProperties", false)
        put("properties", buildJsonObject {
            properties.forEach { (name, type) ->
                put(name, buildJsonObject { put("type", type) })
            }
        })
        put("required", kotlinx.serialization.json.buildJsonArray {
            properties.forEach { add(kotlinx.serialization.json.JsonPrimitive(it.first)) }
        })
    }

    private fun JsonObject.string(name: String) = getValue(name).jsonPrimitive.content
    private fun JsonObject.int(name: String) = getValue(name).jsonPrimitive.int
    private fun JsonObject.double(name: String) = getValue(name).jsonPrimitive.double
}
