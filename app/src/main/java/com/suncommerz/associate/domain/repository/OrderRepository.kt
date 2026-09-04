package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.data.dto.OrderDto
import com.suncommerz.associate.data.dto.OrderItemDto
import com.suncommerz.associate.data.dto.OrderStatusDto
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<OrderDto>>

    fun observeOrder(orderId: String): Flow<OrderDto?>

    suspend fun updateOrder(order: OrderDto)

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatusDto
    )

    suspend fun updateOrderItem(
        orderId: String,
        item: OrderItemDto
    )

}