package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.ProductCategoryDto
import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.ProductCategory

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        price = price,
        currency = currency,
        description = description,
        category = categoryDto.toDomain(),
        ingredients = ingredients,
        attributes = attributes,
    )
}

fun Product.toDto(): ProductDto {
    return ProductDto(
        id = id,
        name = name,
        price = price,
        currency = currency,
        description = description,
        categoryDto = category.toDto(),
        ingredients = ingredients,
        attributes = attributes,
    )
}

fun ProductCategoryDto.toDomain(): ProductCategory =
    when (this) {
        ProductCategoryDto.GROCERY -> ProductCategory.GROCERY
        ProductCategoryDto.BEVERAGE -> ProductCategory.BEVERAGE
        ProductCategoryDto.DAIRY -> ProductCategory.DAIRY
        ProductCategoryDto.MEAT -> ProductCategory.MEAT
        ProductCategoryDto.FRUIT -> ProductCategory.FRUIT
        ProductCategoryDto.VEGETABLE -> ProductCategory.VEGETABLE
        ProductCategoryDto.MEDICINE -> ProductCategory.MEDICINE
        ProductCategoryDto.PERSONAL_CARE -> ProductCategory.PERSONAL_CARE
        ProductCategoryDto.HOUSEHOLD -> ProductCategory.HOUSEHOLD
        ProductCategoryDto.OTHER -> ProductCategory.OTHER
    }

fun ProductCategory.toDto(): ProductCategoryDto =
    when (this) {
        ProductCategory.GROCERY -> ProductCategoryDto.GROCERY
        ProductCategory.BEVERAGE -> ProductCategoryDto.BEVERAGE
        ProductCategory.DAIRY -> ProductCategoryDto.DAIRY
        ProductCategory.MEAT -> ProductCategoryDto.MEAT
        ProductCategory.FRUIT -> ProductCategoryDto.FRUIT
        ProductCategory.VEGETABLE -> ProductCategoryDto.VEGETABLE
        ProductCategory.MEDICINE -> ProductCategoryDto.MEDICINE
        ProductCategory.PERSONAL_CARE -> ProductCategoryDto.PERSONAL_CARE
        ProductCategory.HOUSEHOLD -> ProductCategoryDto.HOUSEHOLD
        ProductCategory.OTHER -> ProductCategoryDto.OTHER
    }