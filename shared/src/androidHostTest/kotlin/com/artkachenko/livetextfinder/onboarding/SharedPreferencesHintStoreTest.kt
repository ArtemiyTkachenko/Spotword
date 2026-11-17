package com.artkachenko.livetextfinder.onboarding

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SharedPreferencesHintStoreTest {
    private val preferences = RuntimeEnvironment.getApplication()
        .getSharedPreferences("onboarding-test", Context.MODE_PRIVATE)

    @Test
    fun aFreshInstallHasSeenNoHints() {
        assertEquals(emptySet<OnboardingHint>(), SharedPreferencesHintStore(preferences).seen())
    }

    @Test
    fun seenHintsSurviveANewStoreInstance() {
        SharedPreferencesHintStore(preferences).markSeen(OnboardingHint.Search)
        SharedPreferencesHintStore(preferences).markSeen(OnboardingHint.Lists)

        assertEquals(
            setOf(OnboardingHint.Search, OnboardingHint.Lists),
            SharedPreferencesHintStore(preferences).seen(),
        )
    }
}
