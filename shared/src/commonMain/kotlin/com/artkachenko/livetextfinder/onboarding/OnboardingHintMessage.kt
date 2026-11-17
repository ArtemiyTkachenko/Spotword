package com.artkachenko.livetextfinder.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.artkachenko.livetextfinder.ui.ContextMessage
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.onboarding_hint_dismiss
import livetextfinder.shared.generated.resources.onboarding_hint_lists
import livetextfinder.shared.generated.resources.onboarding_hint_results
import livetextfinder.shared.generated.resources.onboarding_hint_search
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Shows [hint] as a floating scanner message with a dismiss action. */
@Composable
internal fun OnboardingHintMessage(
    hint: OnboardingHint,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContextMessage(
        message = stringResource(hint.message),
        actionLabel = stringResource(Res.string.onboarding_hint_dismiss),
        onAction = onDismiss,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        floating = true,
    )
}

private val OnboardingHint.message: StringResource
    get() = when (this) {
        OnboardingHint.Search -> Res.string.onboarding_hint_search
        OnboardingHint.Results -> Res.string.onboarding_hint_results
        OnboardingHint.Lists -> Res.string.onboarding_hint_lists
    }
