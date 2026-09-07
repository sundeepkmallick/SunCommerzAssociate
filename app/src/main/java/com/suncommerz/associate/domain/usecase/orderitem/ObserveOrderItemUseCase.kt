package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrderItemUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    fun observeOrderItem(orderId: String, orderItemId: String): Flow<OrderItem?> {
        return orderRepository.observeOrderItem(orderId = orderId, orderItemId = orderItemId)
    }
}