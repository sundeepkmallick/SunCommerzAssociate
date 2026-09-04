package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.data.dto.StoreAssociateDto
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeLoggedInAssociate(): Flow<StoreAssociateDto?>
}