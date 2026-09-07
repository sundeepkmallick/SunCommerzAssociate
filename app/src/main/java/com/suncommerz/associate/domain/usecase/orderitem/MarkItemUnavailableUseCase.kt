package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.repository.OrderRepository
import javax.inject.Inject

class MarkItemUnavailableUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        item: OrderItem,
        notifyManager: Boolean = true,
        notifyCustomer: Boolean = true
    ) {
        val updatedItem = item.copy(
            pickupStatus = ItemPickupStatus.UNAVAILABLE,
            pickedQuantity = 0,
            managerNotified = notifyManager,
            customerNotified = notifyCustomer
        )

        orderRepository.updateOrderItem(
            orderId = orderId,
            item = updatedItem
        )
    }
}