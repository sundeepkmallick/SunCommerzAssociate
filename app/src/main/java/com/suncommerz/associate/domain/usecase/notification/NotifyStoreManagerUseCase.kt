package com.suncommerz.associate.domain.usecase.notification

import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.repository.NotificationRepository
import javax.inject.Inject

class NotifyStoreManagerUseCase @Inject constructor(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke(notification: AssociateNotification){
        notificationRepository.notifyManager(notification)
    }
}