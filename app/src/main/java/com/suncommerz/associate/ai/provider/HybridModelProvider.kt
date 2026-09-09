package com.suncommerz.associate.ai.provider

import com.google.adk.kt.models.Model
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HybridModelProvider @Inject constructor(
    private val firebaseModelProvider: FirebaseModelProvider,
    private val localModelProvider: LocalModelProvider,
) {

    /*fun getModel(): Model {
        return if (localModelProvider.isModelAvailable()) {
            localModelProvider.getModel()
        } else {
            firebaseModelProvider.getModel()
        }
    }*/

    fun getModel(): Model {
        return firebaseModelProvider.getModel()
    }

    fun isLocalModelAvailable(): Boolean {
        return localModelProvider.isModelAvailable()
    }
}
