package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrderItemUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    fun observeOrderItem(orderItemId: String, orderId: String): Flow<OrderItem?> {
        return orderRepository.observeOrderItem(orderItemId = orderItemId, orderId = orderId)
    }
}