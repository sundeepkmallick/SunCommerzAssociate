package com.suncommerz.associate.ai.provider

import com.google.adk.kt.models.Model

interface AIModelProvider {
    suspend fun getModel(): Model
    fun isLocalModelAvailable(): Boolean
}