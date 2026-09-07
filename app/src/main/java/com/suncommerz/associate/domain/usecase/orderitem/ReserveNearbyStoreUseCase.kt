package com.suncommerz.associate.domain.usecase.orderitem

import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.OrderRepository
import javax.inject.Inject

class ReserveNearbyStoreUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        item: OrderItem,
        nearbyStore: Store
    ) {
        val updatedItem = item.copy(
            pickupStatus = ItemPickupStatus.RESERVED_NEARBY_STORE,
            pickedQuantity = 0,
            reservedStoreId = nearbyStore.id
        )

        orderRepository.updateOrderItem(
            orderId = orderId,
            item = updatedItem
        )
    }
}