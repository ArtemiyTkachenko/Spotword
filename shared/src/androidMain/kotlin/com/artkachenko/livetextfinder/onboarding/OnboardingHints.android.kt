package com.artkachenko.livetextfinder.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberOnboardingHintStore(): OnboardingHintStore {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        SharedPreferencesHintStore(
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
        )
    }
}

/** Stores one boolean per hint; a missing key means the hint has not been seen. */
internal class SharedPreferencesHintStore(
    private val preferences: SharedPreferences,
) : OnboardingHintStore {
    override fun seen(): Set<OnboardingHint> =
        OnboardingHint.entries.filterTo(mutableSetOf()) { hint ->
            preferences.getBoolean(key(hint), false)
        }

    override fun markSeen(hint: OnboardingHint) {
        preferences.edit().putBoolean(key(hint), true).apply()
    }

    private fun key(hint: OnboardingHint) = "hint_seen_${hint.name}"
}

private const val PREFERENCES_NAME = "onboarding"
