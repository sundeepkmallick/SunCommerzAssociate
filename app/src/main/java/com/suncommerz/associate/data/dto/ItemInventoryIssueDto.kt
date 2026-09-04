package com.suncommerz.associate.data.dto

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ItemInventoryIssueDto @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val itemId: String,
    val isManagerNotified: Boolean = false,
    val createdAt: Instant,
    val isResolved: Boolean = false
)