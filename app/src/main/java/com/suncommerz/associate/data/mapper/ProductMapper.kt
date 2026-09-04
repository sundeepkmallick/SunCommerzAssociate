package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.domain.model.Product

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        price = price,
        currency = currency,
        description = description
    )
}