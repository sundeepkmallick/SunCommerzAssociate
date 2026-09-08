package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.StoreAssociateDto
import com.suncommerz.associate.domain.model.StoreAssociate

fun StoreAssociateDto.toDomain(): StoreAssociate {
    return StoreAssociate(
        id = id,
        userName = userName
    )
}