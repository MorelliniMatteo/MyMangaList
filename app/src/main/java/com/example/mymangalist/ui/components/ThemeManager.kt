package com.example.mymangalist.ui.components

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        THEME_PREFERENCES,
        Context.MODE_PRIVATE
    )

    private val _currentThemeFlow = MutableStateFlow(false)

    fun isDarkThemeForUser(username: String): StateFlow<Boolean> {
        _currentThemeFlow.value = getUserThemePreference(username)
        return _currentThemeFlow
    }

    private fun getUserThemePreference(username: String): Boolean {
        return sharedPreferences.getBoolean("${THEME_KEY_PREFIX}$username", false)
    }

    suspend fun setDarkTheme(username: String, isDark: Boolean) {
        sharedPreferences.edit().putBoolean("${THEME_KEY_PREFIX}$username", isDark).apply()
        _currentThemeFlow.value = isDark
    }

    companion object {
        private const val THEME_PREFERENCES = "user_theme_preferences"
        private const val THEME_KEY_PREFIX = "dark_theme_"
    }
}