package com.suncommerz.associate.domain.usecase.user

import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<StoreAssociate?> {
        return userRepository.observeLoggedInAssociate()
    }
}