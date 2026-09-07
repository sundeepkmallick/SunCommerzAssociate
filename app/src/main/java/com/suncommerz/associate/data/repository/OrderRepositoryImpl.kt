package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.dto.OrderStatusDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.data.mapper.toDto
import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.SubstitutionHistory
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

    override fun observeOrderItem(
        orderId: String,
        orderItemId: String,
    ): Flow<OrderItem?> {
        return observeOrder(orderId).map { order ->
            order?.items?.find {
                it.id == orderItemId
            }
        }
    }

    override fun pastOrderWithSubstitutionHistory(productId: String): Flow<List<SubstitutionHistory>> {
        return api.substitutionHistory.map { substitutionHistoryDtos ->
                substitutionHistoryDtos.filter {
                    it.originalProductId == productId
                }.map {
                    it.toDomain()
                }
        }
    }


    override suspend fun updateOrder(order: Order) {

    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ) {
        val orderUpdate = api.orders.value.map { order ->
            if (order.id == orderId) {
                order.copy(orderStatusDto = OrderStatusDto.valueOf(status.name))
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
                    item.toDto()
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