package com.suncommerz.associate.domain.usecase.order

import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {

    suspend operator fun invoke(
        orderId: String,
        orderStatus: OrderStatus
    ) {
        orderRepository.updateOrderStatus(
            orderId = orderId,
            status = orderStatus
        )
    }
}