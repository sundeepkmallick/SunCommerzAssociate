package com.suncommerz.associate.data.dto

data class ProductSubstituteDto(
    val productId: String,
    val substituteProductId: String,

    val compatibilityScore: Double,
    val reason: SubstituteReasonDto,
    val source: SubstituteSourceDto
)

enum class SubstituteReasonDto {
    SAME_PRODUCT_TYPE,
    SAME_INGREDIENT,
    SAME_FUNCTION,
    SIMILAR_TASTE,
    SIMILAR_NUTRITION,
    CUSTOMER_PREFERENCE
}

enum class SubstituteSourceDto {
    MANUAL,
    CUSTOMER_HISTORY,
    SYSTEM_GENERATED
}
