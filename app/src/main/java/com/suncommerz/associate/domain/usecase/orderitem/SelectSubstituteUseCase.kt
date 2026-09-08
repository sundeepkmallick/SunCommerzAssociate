package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SelectSubstituteUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        orderItemId: String,
        substituteProductId: String,
        pickedQuantity: Int
    ) {
        require(pickedQuantity >= 0) {
            "Picked quantity cannot be negative"
        }

        val orderItem = orderRepository.observeOrderItem(orderId, orderItemId).first()

        require(pickedQuantity <= orderItem!!.requestedQuantity) {
            "Picked quantity cannot exceed requested quantity"
        }

        val updatedItem = orderItem.copy(
            pickupStatus = ItemPickupStatus.SUBSTITUTE,
            pickedQuantity = pickedQuantity,
            selectedSubstituteId = substituteProductId
        )

        orderRepository.updateOrderItem(
            orderId = orderId,
            item = updatedItem
        )
    }
}