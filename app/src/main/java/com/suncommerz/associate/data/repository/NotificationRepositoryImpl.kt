package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(val api: FakeBackendApiResponse): NotificationRepository{
    override suspend fun notifyManager(notification: AssociateNotification) {
        api.updateNotifications(notification.toDomain())
    }
}