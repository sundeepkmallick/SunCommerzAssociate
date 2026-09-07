package com.suncommerz.associate.domain.model

import kotlin.time.Instant

data class CustomerPurchase(
    val customerId: String,
    val productId: String,
    val quantity: Int,
    val orderDateTime: Instant
)
