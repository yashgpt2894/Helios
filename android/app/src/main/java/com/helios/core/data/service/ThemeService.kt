package com.helios.core.data.service

import kotlinx.coroutines.flow.Flow

/** Appearance modes (SVC-18). Auto follows the system setting. */
enum class HeliosThemeMode { AUTO, LIGHT, DARK }

/**
 * Theme preference (SVC-18). The value must be known before the first frame so the app
 * never flashes the wrong theme; that is why the flow starts as
 * [Loadable.Loading] and the app waits for the first value at startup.
 */
interface ThemeService {

    val themeMode: Flow<Loadable<HeliosThemeMode>>

    suspend fun setTheme(mode: HeliosThemeMode)

    /** True when the resolved theme is dark for this mode and system setting. */
    fun resolveDark(mode: HeliosThemeMode, systemDark: Boolean): Boolean =
        when (mode) {
            HeliosThemeMode.AUTO -> systemDark
            HeliosThemeMode.LIGHT -> false
            HeliosThemeMode.DARK -> true
        }
}
