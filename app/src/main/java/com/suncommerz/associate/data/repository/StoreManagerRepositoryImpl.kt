package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.repository.StoreManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreManagerRepositoryImpl @Inject constructor(val apiResponse: FakeBackendApiResponse) :
    StoreManagerRepository {
    override fun observeStoreManager(): Flow<StoreManager?> {
        return apiResponse.storeManager.map {
            it.toDomain()
        }
    }
}