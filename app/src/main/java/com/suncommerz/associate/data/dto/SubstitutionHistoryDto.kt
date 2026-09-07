package com.suncommerz.associate.data.dto

import kotlin.time.Instant

data class SubstitutionHistoryDto(
    val customerId: String,
    val originalProductId: String,
    val substituteProductId: String,
    val accepted: Boolean,
    val orderDateTime: Instant
)
