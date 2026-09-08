package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.AssociateNotification

interface NotificationRepository {
    suspend fun notifyManager(notification: AssociateNotification)
}