package com.suncommerz.associate.ui.orderitem

import com.suncommerz.associate.domain.model.NotificationReasonType

sealed interface OrderItemDetailsEvent{
    data class ShowToastNotified(val notificationReasonType: NotificationReasonType): OrderItemDetailsEvent
}