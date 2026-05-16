package com.helios.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

/**
 * Theme preference repository persisted via DataStore.
 */
class ThemeRepository(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    enum class ThemeMode { auto, light, dark }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "light" -> ThemeMode.light
            "dark" -> ThemeMode.dark
            else -> ThemeMode.auto
        }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
}
