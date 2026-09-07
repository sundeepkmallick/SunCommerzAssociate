package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.SubstitutionHistory
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>

    fun observeOrder(orderId: String): Flow<Order?>

    fun observeOrderItem(orderId: String, orderItemId: String): Flow<OrderItem?>

    fun pastOrderWithSubstitutionHistory(productId: String): Flow<List<SubstitutionHistory>>

    suspend fun updateOrder(order: Order)

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    )

    suspend fun updateOrderItem(
        orderId: String,
        item: OrderItem
    )

}