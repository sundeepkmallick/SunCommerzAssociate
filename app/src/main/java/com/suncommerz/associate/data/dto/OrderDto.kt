package com.suncommerz.associate.data.dto

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OrderDto @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val status: OrderStatusDto,
    val orderDateTime: Instant,
    val storeDto: StoreDto,
    val items: List<OrderItemDto>,
    val assignedAssociate: StoreAssociateDto
)
