package com.artkachenko.livetextfinder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberAppearancePreference(): AppearancePreference = remember {
    val preferences = NSUserDefaults.standardUserDefaults
    object : AppearancePreference {
        override fun read(): AppAppearance =
            appearanceFromPreference(preferences.stringForKey("appAppearance"))

        override fun write(appearance: AppAppearance) {
            preferences.setObject(appearance.name, forKey = "appAppearance")
        }
    }
}
