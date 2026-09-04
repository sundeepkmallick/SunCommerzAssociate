package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.StoreAssociate
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeLoggedInAssociate(): Flow<StoreAssociate?>
}