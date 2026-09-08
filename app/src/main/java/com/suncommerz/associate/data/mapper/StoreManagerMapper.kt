package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.StoreManagerDto
import com.suncommerz.associate.domain.model.StoreManager

fun StoreManagerDto.toDomain(): StoreManager {
    return StoreManager(
        id = id,
        userName = userName,
        storeId = storeId,
        associates = associatesDto.map { associateDto -> associateDto.toDomain() }
    )
}