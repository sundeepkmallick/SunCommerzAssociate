package com.suncommerz.associate.data.mapper

import com.suncommerz.associate.data.dto.AssociateNotificationDto
import com.suncommerz.associate.data.dto.NotificationReasonTypeDto
import com.suncommerz.associate.domain.model.AssociateNotification
import com.suncommerz.associate.domain.model.NotificationReasonType

fun AssociateNotificationDto.toDomain(): AssociateNotification {
    return AssociateNotification(
        notificationReasonType = NotificationReasonType.valueOf(notificationReasonTypeDto.name),
        orderId = orderId,
        productId = productId,
        reportedByAssociateId = reportedByAssociateId,
        reportedToManagerId = reportedByAssociateId,
        reportDateTime = reportDateTime
    )
}

fun AssociateNotification.toDomain(): AssociateNotificationDto {
    return AssociateNotificationDto(
        notificationReasonTypeDto = NotificationReasonTypeDto.valueOf(notificationReasonType.name),
        orderId = orderId,
        productId = productId,
        reportedByAssociateId = reportedByAssociateId,
        reportedToManagerId = reportedToManagerId,
        reportDateTime = reportDateTime
    )
}