package com.suncommerz.associate.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Order @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val status: OrderStatus,
    val orderDateTime: Instant,
    val store: Store,
    val items: List<OrderItem>,
    val assignedAssociate: StoreAssociate
)
