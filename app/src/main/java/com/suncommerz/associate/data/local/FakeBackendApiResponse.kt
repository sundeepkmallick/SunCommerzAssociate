package com.suncommerz.associate.data.local

import com.suncommerz.associate.data.model.Coordinate
import com.suncommerz.associate.data.model.InventoryRecord
import com.suncommerz.associate.data.model.ItemPickupStatus
import com.suncommerz.associate.data.model.Order
import com.suncommerz.associate.data.model.OrderItem
import com.suncommerz.associate.data.model.OrderStatus
import com.suncommerz.associate.data.model.Product
import com.suncommerz.associate.data.model.Store
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class FakeBackendApiResponse {
    val stores = listOf(
        Store("93", "Store #93", Coordinate(52.5208, 13.4095)),
        Store("94", "Store #94", Coordinate(52.5096, 13.3769)),
        Store("95", "Store #95", Coordinate(52.5322, 13.3849)),
        Store("96", "Store #96", Coordinate(52.4997, 13.4447)),
        Store("97", "Store #97", Coordinate(52.5478, 13.4156)),
        Store("98", "Store #98", Coordinate(52.4839, 13.4331)),
        Store("99", "Store #99", Coordinate(52.5612, 13.3287))
    )

    val assignedStore = storeById("95")

    val products = listOf(
        Product("1", "Milk", 1.19, "EUR", "Fresh whole milk, 1 liter"),
        Product("2", "Bread", 1.49, "EUR", "Freshly baked wheat bread, 500 g"),
        Product("3", "Eggs", 2.49, "EUR", "Free-range eggs, pack of 10"),
        Product("4", "Butter", 2.39, "EUR", "Creamy salted butter, 250 g"),
        Product("5", "Cheese", 3.29, "EUR", "Mild Gouda cheese, 200 g"),
        Product("6", "Apples", 2.49, "EUR", "Fresh red apples, 1 kg"),
        Product("7", "Bananas", 1.69, "EUR", "Fresh bananas, 1 kg"),
        Product("8", "Oranges", 2.29, "EUR", "Juicy oranges, 1 kg"),
        Product("9", "Tomatoes", 2.99, "EUR", "Fresh vine tomatoes, 500 g"),
        Product("10", "Potatoes", 2.49, "EUR", "German potatoes, 2 kg"),
        Product("11", "Carrots", 1.29, "EUR", "Fresh carrots, 1 kg"),
        Product("12", "Onions", 1.39, "EUR", "Yellow onions, 1 kg"),
        Product("13", "Chicken Breast", 6.99, "EUR", "Fresh chicken breast, 500 g"),
        Product("14", "Ground Beef", 5.49, "EUR", "Fresh ground beef, 500 g"),
        Product("15", "Salmon", 8.99, "EUR", "Fresh salmon fillet, 250 g"),
        Product("16", "Rice", 2.19, "EUR", "Long-grain rice, 1 kg"),
        Product("17", "Pasta", 1.29, "EUR", "Durum wheat pasta, 500 g"),
        Product("18", "Olive Oil", 7.49, "EUR", "Extra virgin olive oil, 500 ml"),
        Product("19", "Coffee", 6.99, "EUR", "Ground coffee, 500 g"),
        Product("20", "Orange Juice", 2.49, "EUR", "100% orange juice, 1 liter"),
        Product("21", "Mineral Water", 3.99, "EUR", "Natural mineral water, 6 × 1.5 L"),
        Product("22", "Coca-Cola", 2.29, "EUR", "Coca-Cola, 1.5 liter"),
        Product("23", "Yogurt", 1.79, "EUR", "Natural yogurt, 500 g"),
        Product("24", "Cereal", 3.49, "EUR", "Whole grain breakfast cereal, 500 g"),
        Product("25", "Chocolate", 1.29, "EUR", "Milk chocolate bar, 100 g")
    )


    val inventoryRecords = listOf(
        InventoryRecord("1", "93", 24),
        InventoryRecord("2", "93", 12),
        InventoryRecord("3", "93", 4),
        InventoryRecord("4", "93", 18),
        InventoryRecord("5", "93", 7),
        InventoryRecord("6", "94", 32),
        InventoryRecord("7", "94", 3),
        InventoryRecord("8", "94", 15),
        InventoryRecord("9", "94", 21),
        InventoryRecord("10", "94", 8),
        InventoryRecord("11", "95", 19),
        InventoryRecord("12", "95", 6),
        InventoryRecord("13", "95", 4),
        InventoryRecord("14", "95", 11),
        InventoryRecord("15", "95", 2),
        InventoryRecord("16", "96", 25),
        InventoryRecord("17", "96", 14),
        InventoryRecord("18", "96", 9),
        InventoryRecord("19", "96", 3),
        InventoryRecord("20", "96", 17),
        InventoryRecord("21", "97", 40),
        InventoryRecord("22", "97", 22),
        InventoryRecord("23", "97", 5),
        InventoryRecord("24", "97", 13),
        InventoryRecord("25", "97", 7),
        InventoryRecord("1", "98", 18),
        InventoryRecord("2", "98", 9),
        InventoryRecord("3", "98", 2),
        InventoryRecord("4", "98", 16),
        InventoryRecord("5", "98", 6),
        InventoryRecord("6", "99", 27),
        InventoryRecord("7", "99", 4),
        InventoryRecord("8", "99", 13),
        InventoryRecord("9", "99", 8),
        InventoryRecord("10", "99", 3)
    )

    @OptIn(ExperimentalTime::class)
    val orders = listOf(
        Order(
            id = "ORD-1001",
            status = OrderStatus.READY,
            orderDateTime = Instant.parse("2026-09-04T07:45:00Z"),
            store = Store("93", "Store #93", Coordinate(52.5208, 13.4095)),
            items = listOf(
                OrderItem("OI-1001", products[0], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1002", products[2], 1, ItemPickupStatus.PICKED, 1),
                OrderItem("OI-1003", products[6], 1, ItemPickupStatus.PICKED, 1)
            )
        ),
        Order(
            id = "ORD-1002",
            status = OrderStatus.PICKING,
            orderDateTime = Instant.parse("2026-09-04T08:20:00Z"),
            store = Store("94", "Store #94", Coordinate(52.5096, 13.3769)),
            items = listOf(
                OrderItem("OI-1004", products[5], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1005", products[7], 1, ItemPickupStatus.PENDING),
                OrderItem("OI-1006", products[9], 1, ItemPickupStatus.PENDING)
            )
        ),
        Order(
            id = "ORD-1003",
            status = OrderStatus.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-04T06:30:00Z"),
            store = Store("95", "Store #95", Coordinate(52.5322, 13.3849)),
            items = listOf(
                OrderItem("OI-1007", products[10], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1008", products[12], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1009", products[14], 1, ItemPickupStatus.UNAVAILABLE, 0, managerNotified = true, customerNotified = true)
            )
        ),
        Order(
            id = "ORD-1004",
            status = OrderStatus.PENDING,
            orderDateTime = Instant.parse("2026-09-04T08:55:00Z"),
            store = Store("96", "Store #96", Coordinate(52.4997, 13.4447)),
            items = listOf(
                OrderItem("OI-1010", products[15], 1),
                OrderItem("OI-1011", products[16], 2),
                OrderItem("OI-1012", products[17], 1)
            )
        ),
        Order(
            id = "ORD-1005",
            status = OrderStatus.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-03T17:15:00Z"),
            store = Store("97", "Store #97", Coordinate(52.5478, 13.4156)),
            items = listOf(
                OrderItem("OI-1013", products[20], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1014", products[22], 3, ItemPickupStatus.SUBSTITUTE, 2, selectedSubstituteId = "5"),
                OrderItem("OI-1015", products[23], 1, ItemPickupStatus.UNAVAILABLE, 0, managerNotified = true, customerNotified = true)
            )
        ),
        Order(
            id = "ORD-1006",
            status = OrderStatus.READY,
            orderDateTime = Instant.parse("2026-09-03T14:30:00Z"),
            store = Store("98", "Store #98", Coordinate(52.4839, 13.4331)),
            items = listOf(
                OrderItem("OI-1016", products[1], 1, ItemPickupStatus.PICKED, 1),
                OrderItem("OI-1017", products[3], 1, ItemPickupStatus.PICKED, 1),
                OrderItem("OI-1018", products[4], 1, ItemPickupStatus.PICKED, 1),
                OrderItem("OI-1019", products[8], 2, ItemPickupStatus.PICKED, 2)
            )
        ),
        Order(
            id = "ORD-1007",
            status = OrderStatus.PICKING,
            orderDateTime = Instant.parse("2026-09-03T12:10:00Z"),
            store = Store("99", "Store #99", Coordinate(52.5612, 13.3287)),
            items = listOf(
                OrderItem("OI-1020", products[5], 3, ItemPickupStatus.PICKED, 3),
                OrderItem("OI-1021", products[6], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1022", products[7], 1, ItemPickupStatus.PENDING),
                OrderItem("OI-1023", products[9], 2, ItemPickupStatus.PENDING)
            )
        ),
        Order(
            id = "ORD-1008",
            status = OrderStatus.INCOMPLETE,
            orderDateTime = Instant.parse("2026-09-02T16:40:00Z"),
            store = Store("93", "Store #93", Coordinate(52.5208, 13.4095)),
            items = listOf(
                OrderItem("OI-1024", products[0], 2, ItemPickupStatus.PICKED, 2),
                OrderItem("OI-1025", products[18], 1, ItemPickupStatus.RESERVED_NEARBY_STORE, 0, reservedStoreId = "94"),
                OrderItem("OI-1026", products[19], 1, ItemPickupStatus.PICKED, 1)
            )
        )
    )

    fun productById(id: String): Product? = products.find { it.id == id }
    fun storeById(id: String): Store? = stores.find { it.id == id}
}