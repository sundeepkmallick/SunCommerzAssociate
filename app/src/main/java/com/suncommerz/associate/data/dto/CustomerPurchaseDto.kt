package com.suncommerz.associate.data.dto

import kotlin.time.Instant

data class CustomerPurchaseDto(
    val customerId: String,
    val productId: String,
    val quantity: Int,
    val orderDateTime: Instant
)
