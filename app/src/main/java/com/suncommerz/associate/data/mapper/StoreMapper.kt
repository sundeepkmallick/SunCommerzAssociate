package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.domain.model.Store

fun StoreDto.toDomain(): Store {
    return Store(
        id = id,
        name = name,
        location = location.toDomain()
    )
}