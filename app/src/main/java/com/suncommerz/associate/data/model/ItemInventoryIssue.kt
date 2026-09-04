package com.suncommerz.associate.data.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ItemInventoryIssue @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val itemId: String,
    val isManagerNotified: Boolean = false,
    val createdAt: Instant,
    val isResolved: Boolean = false
)