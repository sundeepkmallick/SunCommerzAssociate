package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class OrderRepositoryImpl @Inject constructor(private val api: FakeBackendApiResponse) : OrderRepository {
    override fun observeOrders(): Flow<List<Order>> {
        return api.orders.map { orderDtos ->
            orderDtos.map { orderDto ->
                orderDto.toDomain()
            }
        }
    }

    override fun observeOrder(orderId: String): Flow<Order?> {
        return api.orders.map { orders ->
            orders.find { it.id == orderId }?.toDomain()
        }
    }


    override suspend fun updateOrder(order: Order) {
        val orderUpdate = api.orders.value.map {
            if (it.id == order.id) {
                order
            } else {
                it
            }
        }

        api.updateOrders(orderUpdate)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ) {
        val orderUpdate = api.orders.value.map { order ->
            if (order.id == orderId) {
                order.copy(orderStatusDto = status)
            } else {
                order
            }
        }
        api.updateOrders(orderUpdate)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateOrderItem(
        orderId: String,
        item: OrderItem
    ) {
        val orderUpdate = api.orders.value.map { order ->
            if (order.id != orderId) {
                return@map order
            }

            val updatedItems = order.items.map { existingItem ->
                if (existingItem.id == item.id) {
                    item
                } else {
                    existingItem
                }
            }

            order.copy(
                items = updatedItems
            )
        }
        api.updateOrders(orderUpdate)
    }

}