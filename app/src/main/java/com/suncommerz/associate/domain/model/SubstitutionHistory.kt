package com.suncommerz.associate.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SubstitutionHistory(
    val customerId: String,
    val originalProductId: String,
    val substituteProductId: String,
    val accepted: Boolean,
    val orderDateTime: Instant
)
