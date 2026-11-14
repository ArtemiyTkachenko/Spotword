package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.ui.icons.RightChevron
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.finder_options_appearance
import livetextfinder.shared.generated.resources.finder_options_help
import livetextfinder.shared.generated.resources.finder_options_title
import livetextfinder.shared.generated.resources.still_reset_view
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FinderOptionsSheet(
    highlightLabel: String,
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
                .padding(bottom = FinderSpacing.majorSection),
            verticalArrangement = Arrangement.spacedBy(FinderSpacing.small),
        ) {
            Text(
                stringResource(Res.string.finder_options_title),
                style = MaterialTheme.typography.titleLarge,
            )
            FinderOptionsActionRow(highlightLabel, onHighlight, enabled = highlightEnabled)
            FinderOptionsActionRow(
                stringResource(Res.string.finder_options_appearance, appearanceLabel),
                onAppearance,
            )
            if (onResetView != null) {
                FinderOptionsActionRow(stringResource(Res.string.still_reset_view), onResetView)
            }
            FinderOptionsActionRow(stringResource(Res.string.finder_options_help), onHelp)
        }
    }
}

@Composable
private fun FinderOptionsActionRow(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = 48.dp),
        contentPadding = PaddingValues(
            horizontal = FinderSpacing.absent,
            vertical = FinderSpacing.absent,
        ),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(FinderSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            RightChevron(Modifier.size(16.dp))
        }
    }
}
