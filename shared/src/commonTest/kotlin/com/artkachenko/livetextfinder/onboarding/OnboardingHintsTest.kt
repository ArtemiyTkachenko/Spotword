package com.artkachenko.livetextfinder.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnboardingHintsTest {
    @Test
    fun noHintShowsWhileTheCameraIsNotRunning() {
        assertNull(
            nextOnboardingHint(
                seen = emptySet(),
                cameraRunning = false,
                hasQuery = false,
                hasMatches = false,
            ),
        )
    }

    @Test
    fun searchHintShowsFirstUntilAQueryIsEntered() {
        assertEquals(
            OnboardingHint.Search,
            next(emptySet(), hasQuery = false, hasMatches = false),
        )
        assertNull(
            next(emptySet(), hasQuery = true, hasMatches = true),
        )
    }

    @Test
    fun resultsHintWaitsForTheFirstMatch() {
        val seen = setOf(OnboardingHint.Search)

        assertNull(next(seen, hasQuery = true, hasMatches = false))
        assertEquals(
            OnboardingHint.Results,
            next(seen, hasQuery = true, hasMatches = true),
        )
    }

    @Test
    fun listsHintFollowsTheResultsHintAndThenNothingShows() {
        val afterResults = setOf(OnboardingHint.Search, OnboardingHint.Results)

        assertEquals(
            OnboardingHint.Lists,
            next(afterResults, hasQuery = true, hasMatches = false),
        )
        assertNull(
            nextOnboardingHint(
                seen = OnboardingHint.entries.toSet(),
                cameraRunning = true,
                hasQuery = false,
                hasMatches = false,
            ),
        )
    }

    @Test
    fun markingAHintSeenWritesItOnceAndAdvancesToTheNext() {
        val store = RecordingHintStore()
        val hints = OnboardingHints(store)

        hints.markSeen(OnboardingHint.Search)
        hints.markSeen(OnboardingHint.Search)

        assertEquals(listOf(OnboardingHint.Search), store.writes)
        assertEquals(
            OnboardingHint.Results,
            hints.next(cameraRunning = true, hasQuery = true, hasMatches = true),
        )
    }

    @Test
    fun hintsSeenInAnEarlierLaunchStayHidden() {
        val hints = OnboardingHints(RecordingHintStore(OnboardingHint.entries.toSet()))

        assertNull(hints.next(cameraRunning = true, hasQuery = false, hasMatches = false))
    }

    private fun next(
        seen: Set<OnboardingHint>,
        hasQuery: Boolean,
        hasMatches: Boolean,
    ): OnboardingHint? = nextOnboardingHint(seen, cameraRunning = true, hasQuery, hasMatches)

    private class RecordingHintStore(
        private val initial: Set<OnboardingHint> = emptySet(),
    ) : OnboardingHintStore {
        val writes = mutableListOf<OnboardingHint>()

        override fun seen(): Set<OnboardingHint> = initial

        override fun markSeen(hint: OnboardingHint) {
            writes += hint
        }
    }
}
