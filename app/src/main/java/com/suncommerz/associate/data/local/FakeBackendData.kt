package com.suncommerz.associate.data.local

import com.suncommerz.associate.data.dto.CoordinateDto
import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.data.dto.ItemPickupStatusDto
import com.suncommerz.associate.data.dto.OrderDto
import com.suncommerz.associate.data.dto.OrderItemDto
import com.suncommerz.associate.data.dto.OrderStatusDto
import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.data.dto.StoreAssociateDto
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

    val products = listOf(
        ProductDto("1", "Milk", 1.19, "EUR", "Fresh whole milk, 1 liter"),
        ProductDto("2", "Bread", 1.49, "EUR", "Freshly baked wheat bread, 500 g"),
        ProductDto("3", "Eggs", 2.49, "EUR", "Free-range eggs, pack of 10"),
        ProductDto("4", "Butter", 2.39, "EUR", "Creamy salted butter, 250 g"),
        ProductDto("5", "Cheese", 3.29, "EUR", "Mild Gouda cheese, 200 g"),
        ProductDto("6", "Apples", 2.49, "EUR", "Fresh red apples, 1 kg"),
        ProductDto("7", "Bananas", 1.69, "EUR", "Fresh bananas, 1 kg"),
        ProductDto("8", "Oranges", 2.29, "EUR", "Juicy oranges, 1 kg"),
        ProductDto("9", "Tomatoes", 2.99, "EUR", "Fresh vine tomatoes, 500 g"),
        ProductDto("10", "Potatoes", 2.49, "EUR", "German potatoes, 2 kg"),
        ProductDto("11", "Carrots", 1.29, "EUR", "Fresh carrots, 1 kg"),
        ProductDto("12", "Onions", 1.39, "EUR", "Yellow onions, 1 kg"),
        ProductDto("13", "Chicken Breast", 6.99, "EUR", "Fresh chicken breast, 500 g"),
        ProductDto("14", "Ground Beef", 5.49, "EUR", "Fresh ground beef, 500 g"),
        ProductDto("15", "Salmon", 8.99, "EUR", "Fresh salmon fillet, 250 g"),
        ProductDto("16", "Rice", 2.19, "EUR", "Long-grain rice, 1 kg"),
        ProductDto("17", "Pasta", 1.29, "EUR", "Durum wheat pasta, 500 g"),
        ProductDto("18", "Olive Oil", 7.49, "EUR", "Extra virgin olive oil, 500 ml"),
        ProductDto("19", "Coffee", 6.99, "EUR", "Ground coffee, 500 g"),
        ProductDto("20", "Orange Juice", 2.49, "EUR", "100% orange juice, 1 liter"),
        ProductDto("21", "Mineral Water", 3.99, "EUR", "Natural mineral water, 6 × 1.5 L"),
        ProductDto("22", "Coca-Cola", 2.29, "EUR", "Coca-Cola, 1.5 liter"),
        ProductDto("23", "Yogurt", 1.79, "EUR", "Natural yogurt, 500 g"),
        ProductDto("24", "Cereal", 3.49, "EUR", "Whole grain breakfast cereal, 500 g"),
        ProductDto("25", "Chocolate", 1.29, "EUR", "Milk chocolate bar, 100 g")
    )


    val inventoryRecords = listOf(
        InventoryRecordDto("1", "93", 24),
        InventoryRecordDto("2", "93", 12),
        InventoryRecordDto("3", "93", 4),
        InventoryRecordDto("4", "93", 18),
        InventoryRecordDto("5", "93", 7),
        InventoryRecordDto("6", "94", 32),
        InventoryRecordDto("7", "94", 3),
        InventoryRecordDto("8", "94", 15),
        InventoryRecordDto("9", "94", 21),
        InventoryRecordDto("10", "94", 8),
        InventoryRecordDto("11", "95", 19),
        InventoryRecordDto("12", "95", 6),
        InventoryRecordDto("13", "95", 4),
        InventoryRecordDto("14", "95", 11),
        InventoryRecordDto("15", "95", 2),
        InventoryRecordDto("16", "96", 25),
        InventoryRecordDto("17", "96", 14),
        InventoryRecordDto("18", "96", 9),
        InventoryRecordDto("19", "96", 3),
        InventoryRecordDto("20", "96", 17),
        InventoryRecordDto("21", "97", 40),
        InventoryRecordDto("22", "97", 22),
        InventoryRecordDto("23", "97", 5),
        InventoryRecordDto("24", "97", 13),
        InventoryRecordDto("25", "97", 7),
        InventoryRecordDto("1", "98", 18),
        InventoryRecordDto("2", "98", 9),
        InventoryRecordDto("3", "98", 2),
        InventoryRecordDto("4", "98", 16),
        InventoryRecordDto("5", "98", 6),
        InventoryRecordDto("6", "99", 27),
        InventoryRecordDto("7", "99", 4),
        InventoryRecordDto("8", "99", 13),
        InventoryRecordDto("9", "99", 8),
        InventoryRecordDto("10", "99", 3)
    )

    @OptIn(ExperimentalTime::class)
    val orders = listOf(
        OrderDto(
            id = "ORD-1001",
            status = OrderStatusDto.READY,
            orderDateTime = Instant.parse("2026-09-04T07:45:00Z"),
            storeDto = StoreDto("93", "Store #93", CoordinateDto(52.5208, 13.4095)),
            items = listOf(
                OrderItemDto("OI-1001", products[0], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto("OI-1002", products[2], 1, ItemPickupStatusDto.PICKED, 1),
                OrderItemDto("OI-1003", products[6], 1, ItemPickupStatusDto.PICKED, 1)
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1002",
            status = OrderStatusDto.PICKING,
            orderDateTime = Instant.parse("2026-09-04T08:20:00Z"),
            storeDto = StoreDto("94", "Store #94", CoordinateDto(52.5096, 13.3769)),
            items = listOf(
                OrderItemDto("OI-1004", products[5], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto("OI-1005", products[7], 1, ItemPickupStatusDto.PENDING),
                OrderItemDto("OI-1006", products[9], 1, ItemPickupStatusDto.PENDING)
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1003",
            status = OrderStatusDto.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-04T06:30:00Z"),
            storeDto = StoreDto("95", "Store #95", CoordinateDto(52.5322, 13.3849)),
            items = listOf(
                OrderItemDto("OI-1007", products[10], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto("OI-1008", products[12], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto(
                    "OI-1009",
                    products[14],
                    1,
                    ItemPickupStatusDto.UNAVAILABLE,
                    0,
                    managerNotified = true,
                    customerNotified = true
                )
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1004",
            status = OrderStatusDto.PENDING,
            orderDateTime = Instant.parse("2026-09-04T08:55:00Z"),
            storeDto = StoreDto("96", "Store #96", CoordinateDto(52.4997, 13.4447)),
            items = listOf(
                OrderItemDto("OI-1010", products[15], 1),
                OrderItemDto("OI-1011", products[16], 2),
                OrderItemDto("OI-1012", products[17], 1)
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1005",
            status = OrderStatusDto.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-03T17:15:00Z"),
            storeDto = StoreDto("97", "Store #97", CoordinateDto(52.5478, 13.4156)),
            items = listOf(
                OrderItemDto("OI-1013", products[20], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto(
                    "OI-1014",
                    products[22],
                    3,
                    ItemPickupStatusDto.SUBSTITUTE,
                    2,
                    selectedSubstituteId = "5"
                ),
                OrderItemDto(
                    "OI-1015",
                    products[23],
                    1,
                    ItemPickupStatusDto.UNAVAILABLE,
                    0,
                    managerNotified = true,
                    customerNotified = true
                )
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1006",
            status = OrderStatusDto.READY,
            orderDateTime = Instant.parse("2026-09-03T14:30:00Z"),
            storeDto = StoreDto("98", "Store #98", CoordinateDto(52.4839, 13.4331)),
            items = listOf(
                OrderItemDto("OI-1016", products[1], 1, ItemPickupStatusDto.PICKED, 1),
                OrderItemDto("OI-1017", products[3], 1, ItemPickupStatusDto.PICKED, 1),
                OrderItemDto("OI-1018", products[4], 1, ItemPickupStatusDto.PICKED, 1),
                OrderItemDto("OI-1019", products[8], 2, ItemPickupStatusDto.PICKED, 2)
            ),
            assignedAssociate = loggedInAssociate
        ),
        OrderDto(
            id = "ORD-1007",
            status = OrderStatusDto.PICKING,
            orderDateTime = Instant.parse("2026-09-03T12:10:00Z"),
            storeDto = StoreDto("99", "Store #99", CoordinateDto(52.5612, 13.3287)),
            items = listOf(
                OrderItemDto("OI-1020", products[5], 3, ItemPickupStatusDto.PICKED, 3),
                OrderItemDto("OI-1021", products[6], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto("OI-1022", products[7], 1, ItemPickupStatusDto.PENDING),
                OrderItemDto("OI-1023", products[9], 2, ItemPickupStatusDto.PENDING)
            ),
            assignedAssociate = StoreAssociateDto("1", "emp001")
        ),
        OrderDto(
            id = "ORD-1008",
            status = OrderStatusDto.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-02T16:40:00Z"),
            storeDto = StoreDto("93", "Store #93", CoordinateDto(52.5208, 13.4095)),
            items = listOf(
                OrderItemDto("OI-1024", products[0], 2, ItemPickupStatusDto.PICKED, 2),
                OrderItemDto(
                    "OI-1025",
                    products[18],
                    1,
                    ItemPickupStatusDto.RESERVED_NEARBY_STORE,
                    0,
                    reservedStoreId = "94"
                ),
                OrderItemDto("OI-1026", products[19], 1, ItemPickupStatusDto.PICKED, 1)
            ),
            assignedAssociate = StoreAssociateDto("2", "emp002")
        )
    )

    fun productById(id: String): ProductDto? = products.find { it.id == id }
    fun storeById(id: String): StoreDto? = stores.find { it.id == id }
    fun getOrderById(orderId: String) {
        orders.find { it.id == orderId }
    }
}