package com.suncommerz.associate.data.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Order @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val status: OrderStatus,
    val orderDateTime: Instant,
    val store: Store,
    val items: List<OrderItem>
)
