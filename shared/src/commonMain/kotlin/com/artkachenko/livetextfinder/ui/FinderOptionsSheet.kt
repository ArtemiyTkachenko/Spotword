package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.appearance_title
import livetextfinder.shared.generated.resources.finder_contrast
import livetextfinder.shared.generated.resources.finder_help
import livetextfinder.shared.generated.resources.finder_highlight
import livetextfinder.shared.generated.resources.finder_options_help
import livetextfinder.shared.generated.resources.finder_options_title
import livetextfinder.shared.generated.resources.finder_reset_view
import livetextfinder.shared.generated.resources.highlight_color_title
import livetextfinder.shared.generated.resources.still_reset_view
import org.jetbrains.compose.resources.stringResource

/** The settings sheet: one group of rows, each showing its current value under its title. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FinderOptionsSheet(
    highlightName: String,
    highlightColor: Color,
    highlightEnabled: Boolean,
    onHighlight: () -> Unit,
    onHelp: () -> Unit,
    onDismissRequest: () -> Unit,
    appearanceLabel: String,
    onAppearance: () -> Unit,
    onResetView: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FinderSpacing.large)
                .padding(bottom = FinderSpacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(FinderSpacing.medium),
        ) {
            FinderSheetTitle(stringResource(Res.string.finder_options_title))
            FinderRowGroup {
                FinderSettingsRow(
                    icon = Res.drawable.finder_highlight,
                    title = stringResource(Res.string.highlight_color_title),
                    onClick = onHighlight,
                    supportingText = highlightName,
                    enabled = highlightEnabled,
                    trailing = { FinderColorSwatch(highlightColor, size = 20.dp) },
                )
                FinderSettingsRow(
                    icon = Res.drawable.finder_contrast,
                    title = stringResource(Res.string.appearance_title),
                    onClick = onAppearance,
                    supportingText = appearanceLabel,
                )
                if (onResetView != null) {
                    FinderSettingsRow(
                        icon = Res.drawable.finder_reset_view,
                        title = stringResource(Res.string.still_reset_view),
                        onClick = onResetView,
                    )
                }
                FinderSettingsRow(
                    icon = Res.drawable.finder_help,
                    title = stringResource(Res.string.finder_options_help),
                    onClick = onHelp,
                )
            }
        }
    }
}
