package com.artkachenko.livetextfinder.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** One-time tips shown over the live camera, in the order a new user meets them. */
internal enum class OnboardingHint {
    /** How to start: type a word and point the camera at text. */
    Search,

    /** Shown on the first live match: where to see the matches listed. */
    Results,

    /** After the first two: searching several words at once with a list. */
    Lists,
}

/** Remembers, across launches, which [OnboardingHint]s the user has already seen. */
internal interface OnboardingHintStore {
    fun seen(): Set<OnboardingHint>

    fun markSeen(hint: OnboardingHint)
}

@Composable
internal expect fun rememberOnboardingHintStore(): OnboardingHintStore

/**
 * Picks the hint to show, or null for none.
 *
 * Hints only appear while the live camera is delivering frames, so they never cover the
 * permission intro, an error or a retained image. At most one is returned at a time.
 */
internal fun nextOnboardingHint(
    seen: Set<OnboardingHint>,
    cameraRunning: Boolean,
    hasQuery: Boolean,
    hasMatches: Boolean,
): OnboardingHint? = when {
    !cameraRunning -> null
    OnboardingHint.Search !in seen -> if (hasQuery) null else OnboardingHint.Search
    OnboardingHint.Results !in seen -> if (hasMatches) OnboardingHint.Results else null
    OnboardingHint.Lists !in seen -> OnboardingHint.Lists
    else -> null
}

/** Observable seen-state for the hints, written through to [store]. */
@Stable
internal class OnboardingHints(private val store: OnboardingHintStore) {
    private var seen by mutableStateOf(store.seen())

    /** The hint for the current scanner state; see [nextOnboardingHint]. */
    fun next(cameraRunning: Boolean, hasQuery: Boolean, hasMatches: Boolean): OnboardingHint? =
        nextOnboardingHint(seen, cameraRunning, hasQuery, hasMatches)

    /** Records [hint] as seen, whether dismissed or acted on, so it never shows again. */
    fun markSeen(hint: OnboardingHint) {
        if (hint in seen) return
        store.markSeen(hint)
        seen = seen + hint
    }
}

@Composable
internal fun rememberOnboardingHints(): OnboardingHints {
    val store = rememberOnboardingHintStore()
    return remember(store) { OnboardingHints(store) }
}
