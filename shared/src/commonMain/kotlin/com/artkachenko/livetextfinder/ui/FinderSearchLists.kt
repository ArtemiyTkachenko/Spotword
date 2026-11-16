package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.search.SavedSearchList
import com.artkachenko.livetextfinder.search.SearchListEditorState
import com.artkachenko.livetextfinder.search.SearchListTransferState
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.finder_add
import livetextfinder.shared.generated.resources.finder_delete
import livetextfinder.shared.generated.resources.finder_edit
import livetextfinder.shared.generated.resources.finder_file_download
import livetextfinder.shared.generated.resources.finder_more_vert
import livetextfinder.shared.generated.resources.scanner_share
import livetextfinder.shared.generated.resources.search_list_delete
import livetextfinder.shared.generated.resources.search_list_edit
import livetextfinder.shared.generated.resources.search_list_export
import livetextfinder.shared.generated.resources.search_list_import
import livetextfinder.shared.generated.resources.search_list_import_short
import livetextfinder.shared.generated.resources.search_list_more_actions
import livetextfinder.shared.generated.resources.search_list_select_accessibility
import livetextfinder.shared.generated.resources.search_list_term_count
import livetextfinder.shared.generated.resources.search_lists_empty
import livetextfinder.shared.generated.resources.search_lists_new
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Saved search lists with their create, import and per-list actions, and the inline editor. */
@Composable
internal fun FinderSearchLists(
    lists: List<SavedSearchList>,
    activeListId: String?,
    editor: SearchListEditorState,
    canSave: Boolean,
    onNew: () -> Unit,
    onSelect: (String) -> Unit,
    onEdit: (SavedSearchList) -> Unit,
    onDelete: (SavedSearchList) -> Unit,
    onNameChange: (String) -> Unit,
    onTermsChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    transferState: SearchListTransferState = SearchListTransferState(),
    onImportList: () -> Unit = { Unit },
    onExportList: (SavedSearchList) -> Unit = { Unit },
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = FinderSpacing.large,
            vertical = FinderSpacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(FinderSpacing.large),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FinderSpacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (editor.isOpen) {
                FinderOutlinedAction(
                    label = stringResource(Res.string.search_lists_new),
                    onClick = onNew,
                    enabled = canSave,
                    icon = Res.drawable.finder_add,
                )
            } else {
                FinderPrimaryAction(
                    label = stringResource(Res.string.search_lists_new),
                    enabled = canSave,
                    icon = Res.drawable.finder_add,
                    onClick = onNew,
                )
            }
            if (transferState.supported) {
                FinderOutlinedAction(
                    label = stringResource(Res.string.search_list_import_short),
                    onClick = onImportList,
                    enabled = transferState.importEnabled,
                    icon = Res.drawable.finder_file_download,
                    accessibilityLabel = stringResource(Res.string.search_list_import),
                )
            }
        }
        if (lists.isEmpty() && !editor.isOpen) {
            Text(
                text = stringResource(Res.string.search_lists_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (lists.isNotEmpty()) {
            FinderRowGroup(modifier = Modifier.selectableGroup()) {
                lists.forEach { list ->
                    SearchListRow(
                        list = list,
                        selected = activeListId == list.id,
                        onSelect = { onSelect(list.id) },
                        onEdit = { onEdit(list) },
                        onDelete = { onDelete(list) },
                        onExport = if (transferState.supported) {
                            { onExportList(list) }
                        } else {
                            null
                        },
                        exportEnabled = transferState.exportEnabled,
                    )
                }
            }
        }
        if (editor.isOpen) {
            SearchListEditor(
                editor = editor,
                canSave = canSave,
                onNameChange = onNameChange,
                onTermsChange = onTermsChange,
                onSave = onSave,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun SearchListRow(
    list: SavedSearchList,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: (() -> Unit)?,
    exportEnabled: Boolean,
) {
    val selectLabel = stringResource(Res.string.search_list_select_accessibility, list.name)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f)
                .heightIn(min = 56.dp)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
                .semantics { contentDescription = selectLabel }
                .padding(horizontal = FinderSpacing.large, vertical = FinderSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(FinderSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = pluralStringResource(
                        Res.plurals.search_list_term_count,
                        list.terms.size,
                        list.terms.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SearchListMenu(
            listName = list.name,
            onEdit = onEdit,
            onDelete = onDelete,
            onExport = onExport,
            exportEnabled = exportEnabled,
        )
    }
}

@Composable
private fun SearchListMenu(
    listName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: (() -> Unit)?,
    exportEnabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    fun closeThen(action: () -> Unit): () -> Unit = {
        expanded = false
        action()
    }
    Box(modifier = Modifier.padding(end = FinderSpacing.extraSmall)) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(Res.drawable.finder_more_vert),
                contentDescription = stringResource(
                    Res.string.search_list_more_actions,
                    listName,
                ),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SearchListMenuItems(
                onEdit = closeThen(onEdit),
                onDelete = closeThen(onDelete),
                onExport = onExport?.let(::closeThen),
                exportEnabled = exportEnabled,
            )
        }
    }
}

/** The per-list actions, one icon and label per line. Export is absent without transfer. */
@Composable
internal fun SearchListMenuItems(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: (() -> Unit)?,
    exportEnabled: Boolean,
) {
    SearchListMenuItem(
        label = stringResource(Res.string.search_list_edit),
        icon = Res.drawable.finder_edit,
        onClick = onEdit,
    )
    if (onExport != null) {
        SearchListMenuItem(
            label = stringResource(Res.string.search_list_export),
            icon = Res.drawable.scanner_share,
            onClick = onExport,
            enabled = exportEnabled,
        )
    }
    SearchListMenuItem(
        label = stringResource(Res.string.search_list_delete),
        icon = Res.drawable.finder_delete,
        onClick = onDelete,
        destructive = true,
    )
}

@Composable
private fun SearchListMenuItem(
    label: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        enabled = enabled,
        leadingIcon = { Icon(painter = painterResource(icon), contentDescription = null) },
        colors = if (destructive) {
            MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error,
            )
        } else {
            MenuDefaults.itemColors()
        },
    )
}
