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

    fun isMobileDataAllowed(): Boolean = preferences.getBoolean(KEY_MOBILE_DATA, false)

    fun setMobileDataAllowed(allowed: Boolean) {
        preferences.edit().putBoolean(KEY_MOBILE_DATA, allowed).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "ai_model_preferences"
        const val KEY_BACKEND = "preferred_backend"
        const val KEY_MOBILE_DATA = "allow_mobile_data"
    }
}
