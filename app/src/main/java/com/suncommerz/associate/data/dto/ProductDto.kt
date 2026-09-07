package com.suncommerz.associate.data.dto

data class ProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val description: String,

    val categoryDto: ProductCategoryDto,
    val ingredients: List<String> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    //val substituteProductIds: List<String> = emptyList()
)

enum class ProductCategoryDto {
    GROCERY,
    BEVERAGE,
    DAIRY,
    MEAT,
    FRUIT,
    VEGETABLE,
    MEDICINE,
    PERSONAL_CARE,
    HOUSEHOLD,
    OTHER
}
