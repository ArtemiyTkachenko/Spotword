package com.artkachenko.livetextfinder

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberAppearancePreference(): AppearancePreference {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        object : AppearancePreference {
            override fun read(): AppAppearance =
                appearanceFromPreference(preferences.getString("theme", null))

            override fun write(appearance: AppAppearance) {
                preferences.edit().putString("theme", appearance.name).apply()
            }
        }
    }
}
