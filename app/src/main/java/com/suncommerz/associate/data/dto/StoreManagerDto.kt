package com.suncommerz.associate.data.dto

data class StoreManagerDto(
    val id: String,
    val userName: String,
    val storeId: String,
    val associatesDto: List<StoreAssociateDto>
)