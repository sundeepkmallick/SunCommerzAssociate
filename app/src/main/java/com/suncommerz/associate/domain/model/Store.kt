package com.suncommerz.associate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String,
    val name: String,
    val location: Coordinate
)