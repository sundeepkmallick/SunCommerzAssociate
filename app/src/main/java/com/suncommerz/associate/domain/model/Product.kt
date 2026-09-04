package com.suncommerz.associate.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val description: String
)