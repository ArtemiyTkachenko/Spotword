package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.AppAppearance
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.appearance_dark
import livetextfinder.shared.generated.resources.appearance_light
import livetextfinder.shared.generated.resources.appearance_system
import livetextfinder.shared.generated.resources.appearance_title
import livetextfinder.shared.generated.resources.status_cancel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun appearanceLabel(appearance: AppAppearance): String = stringResource(
    when (appearance) {
        AppAppearance.System -> Res.string.appearance_system
        AppAppearance.Light -> Res.string.appearance_light
        AppAppearance.Dark -> Res.string.appearance_dark
    },
)

@Composable
internal fun AppearanceDialog(
    selected: AppAppearance,
    onSelect: (AppAppearance) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.appearance_title)) },
        text = {
            Column(Modifier.selectableGroup()) {
                AppAppearance.entries.forEach { appearance ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(
                                selected = appearance == selected,
                                role = Role.RadioButton,
                                onClick = { onSelect(appearance) },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = appearance == selected, onClick = null)
                        Text(appearanceLabel(appearance), Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            FinderDialogAction(stringResource(Res.string.status_cancel), onDismiss)
        },
    )
}
