package com.artkachenko.livetextfinder

import androidx.compose.runtime.Composable

internal enum class AppAppearance(val darkThemeOverride: Boolean?) {
    System(null),
    Light(false),
    Dark(true),
}

internal interface AppearancePreference {
    fun read(): AppAppearance
    fun write(appearance: AppAppearance)
}

internal fun appearanceFromPreference(value: String?): AppAppearance =
    AppAppearance.entries.firstOrNull { it.name == value } ?: AppAppearance.System

@Composable
internal expect fun rememberAppearancePreference(): AppearancePreference
