package com.suncommerz.associate.ai.provider

import com.google.adk.firebase.models.Firebase
import com.google.adk.kt.models.Model
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseModelProvider @Inject constructor() {
    companion object {
        private const val MODEL_NAME = "gemini-flash-latest"
    }

    fun getModel(): Model {

        val firebaseApp = FirebaseApp.getInstance()

        return Firebase.create(
            MODEL_NAME,
            FirebaseAI.getInstance(firebaseApp)
        )
    }
}
