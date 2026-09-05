package com.suncommerz.associate.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object DateTimeUtils {
    private val orderDateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun formatInstantToDateTime(instant: Instant): String {
        return instant.toJavaInstant()
            .atZone(ZoneId.systemDefault())
            .format(orderDateTimeFormatter)
    }
}
