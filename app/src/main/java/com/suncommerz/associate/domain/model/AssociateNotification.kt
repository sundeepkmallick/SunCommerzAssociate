package com.suncommerz.associate.domain.model

import kotlin.time.Instant

data class AssociateNotification (
    val notificationReasonType: NotificationReasonType,
    val orderId: String,
    val productId: String,
    val reportedByAssociateId: String,
    val reportedToManagerId: String,
    val reportDateTime: Instant
)

enum class NotificationReasonType {
    INVENTORY_ISSUE_LOW_STOCK,
    INVENTORY_ISSUE_OUT_OF_STOCK,
    OPERATION_ISSUE_UNABLE_TO_RESERVE,
}