package com.suncommerz.associate.domain.usecase.storemanager

import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import com.suncommerz.associate.domain.repository.StoreManagerRepository
import com.suncommerz.associate.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveStoreManagerUseCase @Inject constructor(
    private val storeManagerRepository: StoreManagerRepository
) {
    suspend operator fun invoke(): Flow<StoreManager?> {
        return storeManagerRepository.observeStoreManager()
    }
}