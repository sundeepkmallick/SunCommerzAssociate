package com.suncommerz.associate.domain.model

import kotlin.time.Instant

data class SubstitutionHistory(
    val customerId: String,
    val originalProductId: String,
    val substituteProductId: String,
    val accepted: Boolean,
    val orderDateTime: Instant
)
