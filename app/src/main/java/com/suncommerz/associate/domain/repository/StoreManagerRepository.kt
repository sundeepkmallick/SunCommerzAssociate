package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.model.StoreManager
import kotlinx.coroutines.flow.Flow

interface StoreManagerRepository {
    fun observeStoreManager(): Flow<StoreManager?>
}