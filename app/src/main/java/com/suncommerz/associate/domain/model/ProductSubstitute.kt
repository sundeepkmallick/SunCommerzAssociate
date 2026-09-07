package com.suncommerz.associate.domain.model

data class ProductSubstitute(
    val productId: String,
    val substituteProductId: String,
    val compatibilityScore: Double,
    val reason: SubstituteReason,
    val source: SubstituteSource
)
