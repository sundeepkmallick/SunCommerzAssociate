package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.repository.OrderRepository
import javax.inject.Inject

class MarkItemPickedUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {

    suspend operator fun invoke(
        orderId: String,
        item: OrderItem,
        pickedQuantity: Int = item.pickedQuantity
    ) {
        require(pickedQuantity >= 0) {
            "Picked quantity cannot be negative"
        }

        require(pickedQuantity <= item.requestedQuantity) {
            "Picked quantity cannot exceed requested quantity"
        }

        val updatedItem = item.copy(
            pickupStatus = ItemPickupStatus.PICKED,
            pickedQuantity = pickedQuantity
        )

        orderRepository.updateOrderItem(
            orderId = orderId,
            item = updatedItem
        )
    }
}