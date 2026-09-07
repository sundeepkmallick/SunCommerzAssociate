package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.OrderRepository
import javax.inject.Inject

class SelectSubstituteUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        item: OrderItem,
        substitute: Product,
        pickedQuantity: Int
    ) {
        require(pickedQuantity >= 0) {
            "Picked quantity cannot be negative"
        }

        require(pickedQuantity <= item.requestedQuantity) {
            "Picked quantity cannot exceed requested quantity"
        }

        val updatedItem = item.copy(
            pickupStatus = ItemPickupStatus.SUBSTITUTE,
            pickedQuantity = pickedQuantity,
            selectedSubstituteId = substitute.id
        )

        orderRepository.updateOrderItem(
            orderId = orderId,
            item = updatedItem
        )
    }
}