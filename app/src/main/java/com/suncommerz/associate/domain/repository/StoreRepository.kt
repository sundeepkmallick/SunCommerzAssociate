package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.Coordinate
import com.suncommerz.associate.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(): Flow<List<Store>>

    fun getStore(
        storeId: String
    ): Flow<Store?>

    suspend fun getNearbyStores(
        locationCurrentStore: Coordinate,
        radiusMeters: Double
    ): List<Store?>
}