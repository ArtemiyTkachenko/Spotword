package com.artkachenko.livetextfinder.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberOnboardingHintStore(): OnboardingHintStore = remember {
    val preferences = NSUserDefaults.standardUserDefaults
    object : OnboardingHintStore {
        override fun seen(): Set<OnboardingHint> =
            OnboardingHint.entries.filterTo(mutableSetOf()) { hint ->
                preferences.boolForKey(key(hint))
            }

        override fun markSeen(hint: OnboardingHint) {
            preferences.setBool(true, forKey = key(hint))
        }

        private fun key(hint: OnboardingHint) = "onboardingHintSeen${hint.name}"
    }
}
