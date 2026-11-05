package com.suncommerz.associate.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the associate's preferred backend. Firebase remains the first-install default. */
@Singleton
class AIModelPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getPreferredBackend(): AIBackend =
        runCatching {
            AIBackend.valueOf(preferences.getString(KEY_BACKEND, AIBackend.FIREBASE.name)!!)
        }.getOrDefault(AIBackend.FIREBASE)

    fun setPreferredBackend(backend: AIBackend) {
        preferences.edit().putString(KEY_BACKEND, backend.name).apply()
    }

    fun getSelectedLocalModelId(): String =
        preferences.getString(KEY_LOCAL_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID)
            ?.takeIf { selected -> LocalModelCatalog.models.any { it.id == selected && it.downloadable } }
            ?: LocalModelCatalog.DEFAULT_MODEL_ID

    fun setSelectedLocalModelId(modelId: String) {
        require(LocalModelCatalog.models.any { it.id == modelId && it.downloadable }) {
            "The selected local model is not available for download in this app build"
        }
        preferences.edit().putString(KEY_LOCAL_MODEL_ID, modelId).apply()
    }

    fun isMobileDataAllowed(): Boolean = preferences.getBoolean(KEY_MOBILE_DATA, false)

    fun setMobileDataAllowed(allowed: Boolean) {
        preferences.edit().putBoolean(KEY_MOBILE_DATA, allowed).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "ai_model_preferences"
        const val KEY_BACKEND = "preferred_backend"
        const val KEY_MOBILE_DATA = "allow_mobile_data"
        const val KEY_LOCAL_MODEL_ID = "selected_local_model_id"
    }
}
