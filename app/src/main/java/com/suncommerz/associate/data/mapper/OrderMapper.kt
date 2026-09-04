package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.ItemPickupStatusDto
import com.suncommerz.associate.data.dto.OrderDto
import com.suncommerz.associate.data.dto.OrderItemDto
import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.Product

fun OrderDto.toDomain(): Order {
    return Order(
        id = id,
        status = OrderStatus.valueOf(orderStatusDto.name),
        orderDateTime = orderDateTime,
        store = storeDto.toDomain(),
        items = items.map { it.toDomain() },
        assignedAssociate = assignedAssociate.toDomain()
    )
}

fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        id = id,
        product = productDto.toDomain(),
        requestedQuantity = requestedQuantity,
        pickupStatus = ItemPickupStatus.valueOf(pickupStatus.name),
        pickedQuantity = pickedQuantity,
        selectedSubstituteId = selectedSubstituteId,
        reservedStoreId = reservedStoreId,
        managerNotified = managerNotified,
        customerNotified = customerNotified
    )
}

fun OrderItem.toDto(): OrderItemDto {
    return OrderItemDto(
        id = id,
        productDto = product.toDto(),
        requestedQuantity = requestedQuantity,
        pickupStatus = ItemPickupStatusDto.valueOf(pickupStatus.name),
        pickedQuantity = pickedQuantity,
        selectedSubstituteId = selectedSubstituteId,
        reservedStoreId = reservedStoreId,
        managerNotified = managerNotified,
        customerNotified = customerNotified
    )
}
