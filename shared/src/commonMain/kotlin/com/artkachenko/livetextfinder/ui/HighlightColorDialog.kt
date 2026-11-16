package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.search.HighlightColor
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.highlight_color_apply
import livetextfinder.shared.generated.resources.highlight_color_cancel
import livetextfinder.shared.generated.resources.highlight_color_custom
import livetextfinder.shared.generated.resources.highlight_color_custom_description
import livetextfinder.shared.generated.resources.highlight_color_cyan
import livetextfinder.shared.generated.resources.highlight_color_done
import livetextfinder.shared.generated.resources.highlight_color_magenta
import livetextfinder.shared.generated.resources.highlight_color_title
import livetextfinder.shared.generated.resources.highlight_color_white
import livetextfinder.shared.generated.resources.highlight_color_yellow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HighlightColorDialog(
    selectedColor: HighlightColor,
    enabled: Boolean,
    onSelectColor: (HighlightColor) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var customEditor by remember { mutableStateOf(false) }
    var red by remember(selectedColor) {
        mutableIntStateOf((selectedColor.argb shr 16 and 0xFF).toInt())
    }
    var green by remember(selectedColor) {
        mutableIntStateOf((selectedColor.argb shr 8 and 0xFF).toInt())
    }
    var blue by remember(selectedColor) { mutableIntStateOf((selectedColor.argb and 0xFF).toInt()) }
    val draft = HighlightColor.fromRgb(red, green, blue)
    val isCustomSelected = HighlightColor.entries.none { it == selectedColor }

    fun resetDraftToSelection() {
        red = (selectedColor.argb shr 16 and 0xFF).toInt()
        green = (selectedColor.argb shr 8 and 0xFF).toInt()
        blue = (selectedColor.argb and 0xFF).toInt()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.highlight_color_title)) },
        text = {
            if (customEditor) {
                CustomColorEditor(
                    red = red,
                    green = green,
                    blue = blue,
                    color = draft,
                    enabled = enabled,
                    onRedChange = { red = it },
                    onGreenChange = { green = it },
                    onBlueChange = { blue = it },
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(FinderSpacing.small),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                        HighlightColor.entries.forEach { color ->
                            val name = localizedHighlightColorName(color)
                            HighlightColorOption(
                                label = name,
                                accessibilityLabel = name,
                                color = color,
                                selected = selectedColor == color,
                                enabled = enabled,
                                onClick = { onSelectColor(color) },
                            )
                        }
                        CustomColorRow(
                            color = selectedColor,
                            selected = isCustomSelected,
                            enabled = enabled,
                            onClick = {
                                resetDraftToSelection()
                                customEditor = true
                            },
                        )
                    }
                    Text(
                        text = stringResource(Res.string.highlight_color_custom_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        dismissButton = if (customEditor) {
            {
                FinderDialogAction(
                    label = stringResource(Res.string.highlight_color_cancel),
                    onClick = {
                        resetDraftToSelection()
                        customEditor = false
                    },
                )
            }
        } else {
            null
        },
        confirmButton = {
            if (customEditor) {
                FinderDialogAction(
                    label = stringResource(Res.string.highlight_color_apply),
                    enabled = enabled,
                    onClick = {
                        onSelectColor(draft)
                        onDismissRequest()
                    },
                )
            } else {
                FinderDialogAction(
                    label = stringResource(Res.string.highlight_color_done),
                    onClick = onDismissRequest,
                )
            }
        },
    )
}

@Composable
internal fun localizedHighlightColorName(color: HighlightColor): String =
    stringResource(color.resourceLabel())

private fun HighlightColor.resourceLabel() = when (argb) {
    HighlightColor.Yellow.argb -> Res.string.highlight_color_yellow
    HighlightColor.Cyan.argb -> Res.string.highlight_color_cyan
    HighlightColor.Magenta.argb -> Res.string.highlight_color_magenta
    HighlightColor.White.argb -> Res.string.highlight_color_white
    else -> Res.string.highlight_color_custom
}
