package com.suncommerz.associate.domain.usecase.store

import com.suncommerz.associate.domain.model.Coordinate
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.repository.StoreRepository
import javax.inject.Inject

class GetNearbyStoresUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        currentStoreLocation: Coordinate,
        radiusMeters: Double
    ): List<Store> {

        require(radiusMeters >= 0) {
            "Radius cannot be negative"
        }

        return storeRepository.getNearbyStores(
            locationCurrentStore = currentStoreLocation,
            radiusMeters = radiusMeters
        ).filterNotNull()
    }
}