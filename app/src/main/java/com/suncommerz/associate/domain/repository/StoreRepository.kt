package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.data.dto.CoordinateDto
import com.suncommerz.associate.data.dto.StoreDto
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(): Flow<List<StoreDto>>

    fun getStore(
        storeId: String
    ): Flow<StoreDto?>

    suspend fun getNearbyStores(
        locationCurrentStore: CoordinateDto,
        radiusMeters: Double
    ): List<StoreDto>
}