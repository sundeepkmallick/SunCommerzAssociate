package com.suncommerz.associate.data.local

import com.suncommerz.associate.data.dto.AssociateNotificationDto
import com.suncommerz.associate.data.dto.CoordinateDto
import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.data.dto.ItemPickupStatusDto
import com.suncommerz.associate.data.dto.NotificationReasonTypeDto
import com.suncommerz.associate.data.dto.OrderDto
import com.suncommerz.associate.data.dto.OrderItemDto
import com.suncommerz.associate.data.dto.OrderStatusDto
import com.suncommerz.associate.data.dto.ProductCategoryDto
import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.data.dto.StoreAssociateDto
import com.suncommerz.associate.data.dto.StoreManagerDto
import com.suncommerz.associate.data.dto.SubstitutionHistoryDto
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object FakeBackendData {

    val stores = listOf(
        StoreDto("93", "Store #93", CoordinateDto(52.5208, 13.4095)),
        StoreDto("94", "Store #94", CoordinateDto(52.5096, 13.3769)),
        StoreDto("95", "Store #95", CoordinateDto(52.5322, 13.3849)),
        StoreDto("96", "Store #96", CoordinateDto(52.4997, 13.4447)),
        StoreDto("97", "Store #97", CoordinateDto(52.5478, 13.4156)),
        StoreDto("98", "Store #98", CoordinateDto(52.4839, 13.4331)),
        StoreDto("99", "Store #99", CoordinateDto(52.5612, 13.3287))
    )

    val assignedStore = storeById("95")
    val loggedInAssociate = StoreAssociateDto("7", "emp007")

    val storeManager = StoreManagerDto("3", "manager003", assignedStore?.id ?: "95",
        listOf(
            StoreAssociateDto("7", "emp007"),
            StoreAssociateDto("1", "emp001"),
            StoreAssociateDto("2", "emp002")
        )
    )

    val notifications = listOf(
        AssociateNotificationDto(
            NotificationReasonTypeDto.INVENTORY_ISSUE_OUT_OF_STOCK,
            "ORD-990",
            "OI-102",
            "1",
            "3",
            Instant.parse("2026-07-18T11:10:00Z")
            )
    )

    val products = listOf(
        // 1 - Original product has substitutes
        ProductDto(
            id = "1",
            name = "Whole Milk",
            price = 1.19,
            currency = "EUR",
            description = "Fresh whole milk, 1 liter",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf("Milk"),
            attributes = mapOf(
                "volume" to "1L",
                "fat" to "3.5%",
                "type" to "whole-milk"
            ),
            //substituteProductIds = listOf("2", "3", "4")
        ),

        // 2 - Substitute for Whole Milk
        ProductDto(
            id = "2",
            name = "Lactose-Free Milk",
            price = 1.49,
            currency = "EUR",
            description = "Lactose-free whole milk, 1 liter",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf("Milk", "Lactase"),
            attributes = mapOf(
                "volume" to "1L",
                "fat" to "3.5%",
                "type" to "lactose-free-milk"
            ),
            //substituteProductIds = listOf("1", "3", "4")
        ),

        // 3 - Substitute for Whole Milk
        ProductDto(
            id = "3",
            name = "Oat Milk",
            price = 2.29,
            currency = "EUR",
            description = "Plant-based oat drink, 1 liter",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf(
                "Water",
                "Oats",
                "Sunflower Oil",
                "Salt"
            ),
            attributes = mapOf(
                "volume" to "1L",
                "type" to "plant-based-milk",
                "diet" to "vegan"
            ),
            //substituteProductIds = listOf("1", "2", "4")
        ),

        // 4 - Substitute for Whole Milk
        ProductDto(
            id = "4",
            name = "Soy Milk",
            price = 2.19,
            currency = "EUR",
            description = "Plant-based soy drink, 1 liter",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf(
                "Water",
                "Soybeans",
                "Calcium",
                "Salt"
            ),
            attributes = mapOf(
                "volume" to "1L",
                "type" to "plant-based-milk",
                "diet" to "vegan"
            ),
            //substituteProductIds = listOf("1", "2", "3")
        ),

        // 5 - Original product with substitute
        ProductDto(
            id = "5",
            name = "Butter",
            price = 2.39,
            currency = "EUR",
            description = "Creamy salted butter, 250 g",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf("Cream", "Salt"),
            attributes = mapOf(
                "weight" to "250g",
                "type" to "butter",
                "salted" to "true"
            ),
            //substituteProductIds = listOf("6")
        ),

        // 6 - Substitute for Butter
        ProductDto(
            id = "6",
            name = "Margarine",
            price = 1.79,
            currency = "EUR",
            description = "Soft vegetable margarine, 250 g",
            categoryDto = ProductCategoryDto.DAIRY,
            ingredients = listOf(
                "Vegetable Oils",
                "Water",
                "Salt"
            ),
            attributes = mapOf(
                "weight" to "250g",
                "type" to "butter-alternative"
            ),
            //substituteProductIds = listOf("5")
        ),

        // 7 - Product that can be unavailable in one store
        ProductDto(
            id = "7",
            name = "Ground Coffee",
            price = 6.99,
            currency = "EUR",
            description = "Ground coffee, 500 g",
            categoryDto = ProductCategoryDto.GROCERY,
            ingredients = listOf("Roasted Coffee Beans"),
            attributes = mapOf(
                "weight" to "500g",
                "type" to "ground-coffee",
                "caffeine" to "regular"
            ),
            //substituteProductIds = listOf("8")
        ),

        // 8 - Coffee substitute
        ProductDto(
            id = "8",
            name = "Decaf Coffee",
            price = 7.49,
            currency = "EUR",
            description = "Ground decaffeinated coffee, 500 g",
            categoryDto = ProductCategoryDto.GROCERY,
            ingredients = listOf(
                "Decaffeinated Roasted Coffee Beans"
            ),
            attributes = mapOf(
                "weight" to "500g",
                "type" to "ground-coffee",
                "caffeine" to "decaf"
            ),
            //substituteProductIds = listOf("7")
        ),

        // 9 - Original product that may need nearby-store search
        ProductDto(
            id = "9",
            name = "Pasta",
            price = 1.29,
            currency = "EUR",
            description = "Durum wheat pasta, 500 g",
            categoryDto = ProductCategoryDto.GROCERY,
            ingredients = listOf(
                "Durum Wheat Semolina",
                "Water"
            ),
            attributes = mapOf(
                "weight" to "500g",
                "type" to "regular-pasta"
            ),
            //substituteProductIds = listOf("10")
        ),

        // 10 - Pasta substitute
        ProductDto(
            id = "10",
            name = "Gluten-Free Pasta",
            price = 2.49,
            currency = "EUR",
            description = "Gluten-free corn and rice pasta, 500 g",
            categoryDto = ProductCategoryDto.GROCERY,
            ingredients = listOf(
                "Corn Flour",
                "Rice Flour",
                "Water"
            ),
            attributes = mapOf(
                "weight" to "500g",
                "type" to "gluten-free-pasta"
            ),
            //substituteProductIds = listOf("9")
        ),

        // 11 - Product intentionally unavailable at assigned store
        // but available at a nearby store.
        ProductDto(
            id = "11",
            name = "Salmon Fillet",
            price = 8.99,
            currency = "EUR",
            description = "Fresh salmon fillet, 250 g",
            categoryDto = ProductCategoryDto.MEAT,
            ingredients = listOf("Salmon"),
            attributes = mapOf(
                "weight" to "250g",
                "type" to "fresh-fish"
            ),
            //substituteProductIds = emptyList()
        ),

        // 12 - Product intentionally unavailable at assigned store
        // but available at another nearby store.
        ProductDto(
            id = "12",
            name = "Chicken Breast",
            price = 6.99,
            currency = "EUR",
            description = "Fresh chicken breast, 500 g",
            categoryDto = ProductCategoryDto.MEAT,
            ingredients = listOf("Chicken"),
            attributes = mapOf(
                "weight" to "500g",
                "type" to "fresh-chicken"
            ),
            //substituteProductIds = emptyList()
        )
    )

    val inventoryRecords = listOf(

        // ---------------------------------------------------------
        // Store #93
        // ---------------------------------------------------------

        InventoryRecordDto("1", "93", 0),   // Whole Milk unavailable
        InventoryRecordDto("2", "93", 8),   // Lactose-Free Milk
        InventoryRecordDto("3", "93", 12),  // Oat Milk
        InventoryRecordDto("4", "93", 4),   // Soy Milk

        // ---------------------------------------------------------
        // Store #94
        // ---------------------------------------------------------

        InventoryRecordDto("5", "94", 0),   // Butter unavailable
        InventoryRecordDto("6", "94", 15),  // Margarine available

        InventoryRecordDto("7", "94", 0),   // Coffee unavailable
        InventoryRecordDto("8", "94", 6),   // Decaf Coffee available
        InventoryRecordDto("9", "94", 12),  // Pasta available nearby

        // ---------------------------------------------------------
        // Store #95 - ASSIGNED STORE
        // ---------------------------------------------------------

        // Substitution scenario
        InventoryRecordDto("1", "95", 0),   // Milk unavailable
        InventoryRecordDto("2", "95", 2),   // Lactose-Free Milk available
        InventoryRecordDto("3", "95", 1),   // Oat Milk available - Low Stock
        InventoryRecordDto("4", "95", 4),   // Soy Milk available

        // Nearby-store scenario
        InventoryRecordDto("9", "95", 0),   // Pasta unavailable
        InventoryRecordDto("10", "95", 0),  // Substitute ALSO unavailable

        InventoryRecordDto("11", "95", 0),  // Salmon unavailable
        InventoryRecordDto("12", "95", 0),  // Chicken unavailable

        // ---------------------------------------------------------
        // Store #96 - Nearby store
        // ---------------------------------------------------------

        InventoryRecordDto("9", "96", 12),  // Pasta available nearby
        InventoryRecordDto("10", "96", 5),   // Gluten-Free Pasta

        InventoryRecordDto("11", "96", 4),   // Salmon available nearby
        InventoryRecordDto("12", "96", 0),   // Chicken unavailable here

        // ---------------------------------------------------------
        // Store #97 - Another nearby store
        // ---------------------------------------------------------

        InventoryRecordDto("11", "97", 0),   // Salmon unavailable
        InventoryRecordDto("12", "97", 8),   // Chicken available nearby
        InventoryRecordDto("9", "97", 18),  // Pasta available nearby

        // ---------------------------------------------------------
        // Store #98
        // ---------------------------------------------------------

        InventoryRecordDto("7", "98", 10),  // Coffee
        InventoryRecordDto("8", "98", 3),   // Decaf Coffee

        // ---------------------------------------------------------
        // Store #99
        // ---------------------------------------------------------

        InventoryRecordDto("5", "99", 8),   // Butter
        InventoryRecordDto("6", "99", 5)    // Margarine
    )



    /**
     * Historical substitution behavior.
     *
     * This data can be used to demonstrate personalized recommendations.
     */
    val substitutionHistory = listOf(

        // Customer 101 usually accepts Oat Milk when Milk is unavailable.
        SubstitutionHistoryDto(
            customerId = "101",
            originalProductId = "1",
            substituteProductId = "3",
            accepted = true,
            orderDateTime = Instant.parse("2026-08-20T09:15:00Z")
        ),
        SubstitutionHistoryDto(
            customerId = "101",
            originalProductId = "1",
            substituteProductId = "3",
            accepted = true,
            orderDateTime = Instant.parse("2026-08-27T10:20:00Z")
        ),
        SubstitutionHistoryDto(
            customerId = "101",
            originalProductId = "1",
            substituteProductId = "2",
            accepted = false,
            orderDateTime = Instant.parse("2026-08-30T11:10:00Z")
        ),

        // Customer 102 prefers Margarine as a Butter substitute.
        SubstitutionHistoryDto(
            customerId = "102",
            originalProductId = "5",
            substituteProductId = "6",
            accepted = true,
            orderDateTime = Instant.parse("2026-08-22T08:30:00Z")
        ),
        SubstitutionHistoryDto(
            customerId = "102",
            originalProductId = "5",
            substituteProductId = "6",
            accepted = true,
            orderDateTime = Instant.parse("2026-08-29T09:45:00Z")
        ),

        // Customer 103 accepted Whole Grain Bread previously.
        SubstitutionHistoryDto(
            customerId = "103",
            originalProductId = "7",
            substituteProductId = "8",
            accepted = true,
            orderDateTime = Instant.parse("2026-08-25T14:20:00Z")
        )
    )

    /**
     * Orders demonstrate different substitution scenarios.
     */
    @OptIn(ExperimentalTime::class)
    val orders = listOf(

        // ---------------------------------------------------------
        // Order 1001 - Milk unavailable, Oat Milk substituted
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1001",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-04T07:45:00Z"),
            storeDto = storeById(assignedStore!!.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1001",
                    orderIdDto = "ORD-1001",
                    productDto = products[0], // Milk
                    quantity = 2,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 0,//2
                    selectedSubstituteId = null //"3"
                ),
                OrderItemDto(
                    id = "OI-1002",
                    orderIdDto = "ORD-1001",
                    productDto = products[7], // Whole Grain Bread
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 0
                )
            ),
            assignedAssociate = loggedInAssociate
        ),

        // ---------------------------------------------------------
        // Order 1002 - Butter unavailable, Margarine substituted
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1002",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-04T08:20:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1003",
                    orderIdDto = "ORD-1002",
                    productDto = products[4], // Butter
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1,
                    selectedSubstituteId = "6"
                ),
                OrderItemDto(
                    id = "OI-1004",
                    orderIdDto = "ORD-1002",
                    productDto = products[6], // White Bread
                    quantity = 2,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 2
                )
            ),
            assignedAssociate = loggedInAssociate
        ),

        // ---------------------------------------------------------
        // Order 1003 - Pasta unavailable, Gluten-Free Pasta offered
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1003",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-04T06:30:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1005",
                    orderIdDto = "ORD-1003",
                    productDto = products[8], // Pasta
                    quantity = 2,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 2,
                    selectedSubstituteId = "10",
                    managerNotified = true,
                    customerNotified = true
                ),
                OrderItemDto(
                    id = "OI-1006",
                    orderIdDto = "ORD-1003",
                    productDto = products[10], // Coffee
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1
                )
            ),
            assignedAssociate = loggedInAssociate
        ),

        // ---------------------------------------------------------
        // Order 1004 - Decaf Coffee unavailable and no substitute
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1004",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-04T08:55:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1007",
                    orderIdDto = "ORD-1004",
                    productDto = products[11], // Decaf Coffee
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 0,
                    managerNotified = true,
                    customerNotified = true
                ),
                OrderItemDto(
                    id = "OI-1008",
                    orderIdDto = "ORD-1004",
                    productDto = products[10], // Coffee
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1
                )
            ),
            assignedAssociate = loggedInAssociate
        ),

        // ---------------------------------------------------------
        // Order 1005 - All items available
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1005",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-03T17:15:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1009",
                    orderIdDto = "ORD-1005",
                    productDto = products[1], // Lactose-Free Milk
                    quantity = 2,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 2
                ),
                OrderItemDto(
                    id = "OI-1010",
                    orderIdDto = "ORD-1005",
                    productDto = products[2], // Oat Milk
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1
                )
            ),
            assignedAssociate = loggedInAssociate
        ),

        OrderDto(
            id = "ORD-1006",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-03T14:30:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1011",
                    orderIdDto = "ORD-1006",
                    productDto = products[6], // White Bread
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1,
                    selectedSubstituteId = "8"
                ),
                OrderItemDto(
                    id = "OI-1012",
                    orderIdDto = "ORD-1006",
                    productDto = products[5], // Margarine
                    quantity = 1,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 1
                )
            ),
            assignedAssociate = StoreAssociateDto("1", "emp001")
        ),

        // ---------------------------------------------------------
        // Order 1007 - Customer accepted Milk -> Oat Milk
        // ---------------------------------------------------------
        OrderDto(
            id = "ORD-1007",
            orderStatusDto = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-02T12:10:00Z"),
            storeDto = storeById(assignedStore.id)!!,
            items = listOf(
                OrderItemDto(
                    id = "OI-1013",
                    orderIdDto = "ORD-1007",
                    productDto = products[0],
                    quantity = 3,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 0,
                    selectedSubstituteId = null
                ),
                OrderItemDto(
                    id = "OI-1014",
                    orderIdDto = "ORD-1007",
                    productDto = products[7],
                    quantity = 2,
                    pickupStatus = ItemPickupStatusDto.PENDING,
                    pickedQuantity = 0
                )
            ),
            assignedAssociate = StoreAssociateDto("2", "emp002")
        )
    )

    fun productById(id: String): ProductDto? =
        products.find { it.id == id }

    fun storeById(id: String): StoreDto? =
        stores.find { it.id == id }

    fun getOrderById(orderId: String): OrderDto? =
        orders.find { it.id == orderId }
}
