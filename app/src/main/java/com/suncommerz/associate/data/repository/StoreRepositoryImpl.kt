package com.suncommerz.associate.data.repository

import android.location.Location
import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.domain.model.Coordinate
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(val apiResponse: FakeBackendApiResponse): StoreRepository {
    override fun observeStores(): Flow<List<Store>> {
        return apiResponse.stores.map { storeDtos ->
            storeDtos.map { storeDto ->
                storeDto.toDomain()
            }
        }
    }

    override fun getStore(storeId: String): Flow<Store?> {
        return apiResponse.stores.map { stores ->
            stores.find { it.id == storeId }?.toDomain()
        }
    }

    override suspend fun getNearbyStores(
        locationCurrentStore: Coordinate,
        radiusMeters: Double
    ): List<StoreDto> {
        return apiResponse.stores.value.filter { store ->
            calculateDistance(locationCurrentStore, store.toDomain().location) <= radiusMeters
        }
    }

    fun calculateDistance(
        currentStore: Coordinate,
        otherStore: Coordinate
    ): Float {
        val result = FloatArray(1)

        Location.distanceBetween(
            currentStore.latitude,
            currentStore.longitude,
            otherStore.latitude,
            otherStore.longitude,
            result
        )

        return result[0]
    }
}