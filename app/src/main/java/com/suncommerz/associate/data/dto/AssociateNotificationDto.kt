package com.suncommerz.associate.data.dto

import kotlin.time.Instant

data class AssociateNotificationDto (
    val notificationReasonTypeDto: NotificationReasonTypeDto,
    val orderId: String,
    val productId: String,
    val reportedByAssociateId: String,
    val reportedToManagerId: String,
    val reportDateTime: Instant
)

enum class NotificationReasonTypeDto {
    INVENTORY_ISSUE_LOW_STOCK,
    INVENTORY_ISSUE_OUT_OF_STOCK,
    OPERATION_ISSUE_UNABLE_TO_RESERVE,
}