package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MarkItemUnavailableUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        orderItemId: String,
        notifyManager: Boolean = true,
        notifyCustomer: Boolean = true
    ) {
        val orderItem = orderRepository.observeOrderItem(orderId, orderItemId).first()

        val updatedItem = orderItem?.copy(
            pickupStatus = ItemPickupStatus.UNAVAILABLE,
            pickedQuantity = 0,
            managerNotified = notifyManager,
            customerNotified = notifyCustomer
        )

        updatedItem?.let {
            orderRepository.updateOrderItem(
                orderId = orderId,
                item = updatedItem
            )
        }

    }
}