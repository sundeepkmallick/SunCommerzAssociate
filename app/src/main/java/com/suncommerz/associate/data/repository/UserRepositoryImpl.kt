package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.dto.StoreAssociateDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(val apiResponse: FakeBackendApiResponse): UserRepository {

    override fun observeLoggedInAssociate(): Flow<StoreAssociateDto?> {
        return apiResponse.loggedInAssociates
    }
}