package com.suncommerz.associate.domain.model

data class StoreManager(
    val id: String,
    val userName: String,
    val storeId: String,
    val associates: List<StoreAssociate>
)