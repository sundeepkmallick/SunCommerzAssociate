package com.suncommerz.associate.domain.usecase.orders

import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(): Flow<List<Order>> {
        return orderRepository.observeOrders()
    }
}