package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MarkItemStockLowUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        orderItemId: String,
        notifyManager: Boolean = true,
    ) {
        val orderItem = orderRepository.observeOrderItem(orderId, orderItemId).first()

        val updatedItem = orderItem?.copy(
            managerNotified = notifyManager,
        )

        updatedItem?.let {
            orderRepository.updateOrderItem(
                orderId = orderId,
                item = updatedItem
            )
        }

    }
}