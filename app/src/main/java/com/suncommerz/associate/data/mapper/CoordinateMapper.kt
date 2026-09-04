package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.CoordinateDto
import com.suncommerz.associate.domain.model.Coordinate

fun CoordinateDto.toDomain(): Coordinate {
    return Coordinate(
        latitude = latitude,
        longitude = longitude
    )
}