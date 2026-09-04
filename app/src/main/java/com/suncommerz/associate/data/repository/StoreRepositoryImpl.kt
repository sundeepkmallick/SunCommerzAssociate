package com.suncommerz.associate.data.repository

import android.location.Location
import com.suncommerz.associate.data.dto.CoordinateDto
import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(val apiResponse: FakeBackendApiResponse): StoreRepository {
    override fun observeStores(): Flow<List<StoreDto>> {
        return apiResponse.stores
    }

    override fun getStore(storeId: String): Flow<StoreDto?> {
        return apiResponse.stores.map { stores ->
            stores.find { it.id == storeId }
        }
    }

    override suspend fun getNearbyStores(
        locationCurrentStore: CoordinateDto,
        radiusMeters: Double
    ): List<StoreDto> {
        return apiResponse.stores.value.filter { store ->
            calculateDistance(locationCurrentStore, store.location) <= radiusMeters
        }
    }

    fun calculateDistance(
        currentStore: CoordinateDto,
        otherStore: CoordinateDto
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