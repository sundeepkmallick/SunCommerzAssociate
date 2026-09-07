package com.suncommerz.associate.domain.usecase.order

import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {

    operator fun invoke(
        orderId: String
    ): Flow<Order?> {
        return orderRepository.observeOrder(
            orderId = orderId
        )
    }
}