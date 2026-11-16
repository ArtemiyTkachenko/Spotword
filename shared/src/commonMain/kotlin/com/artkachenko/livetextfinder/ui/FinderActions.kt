package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * The single high-emphasis action of a surface.
 *
 * Uses the Material filled button: a 40dp pill inside a 48dp touch target. Spacing around the
 * button belongs to the caller's layout.
 */
@Composable
internal fun FinderPrimaryAction(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    icon: DrawableResource? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = actionContentPadding(icon),
        colors = if (destructive) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
    ) {
        ActionContent(label, icon)
    }
}

/** A medium-emphasis action shown beside a [FinderPrimaryAction]. */
@Composable
internal fun FinderOutlinedAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: DrawableResource? = null,
    accessibilityLabel: String? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.describedAs(accessibilityLabel),
        contentPadding = actionContentPadding(icon),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        ActionContent(label, icon)
    }
}

/** A low-emphasis text action inside content, such as Cancel beside Save. */
@Composable
internal fun FinderSecondaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String? = null,
    destructive: Boolean = false,
    floating: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.describedAs(accessibilityLabel),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (floating) {
                FinderCameraOverlay.onControlScrim
            } else if (destructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
    ) {
        Text(label)
    }
}

/** A confirm or dismiss action of an alert dialog, which Material renders as a text button. */
@Composable
internal fun FinderDialogAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = if (destructive) {
            ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.textButtonColors()
        },
    ) {
        Text(label)
    }
}

@Composable
private fun ActionContent(label: String, icon: DrawableResource?) {
    if (icon != null) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    }
    Text(label)
}

private fun actionContentPadding(icon: DrawableResource?) = if (icon == null) {
    ButtonDefaults.ContentPadding
} else {
    ButtonDefaults.ButtonWithIconContentPadding
}

private fun Modifier.describedAs(accessibilityLabel: String?): Modifier =
    if (accessibilityLabel == null) {
        this
    } else {
        semantics { contentDescription = accessibilityLabel }
    }
