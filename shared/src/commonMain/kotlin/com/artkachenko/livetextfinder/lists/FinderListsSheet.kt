package com.artkachenko.livetextfinder.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.search.SearchListTransferOwner
import com.artkachenko.livetextfinder.search.SearchSessionState
import com.artkachenko.livetextfinder.ui.ContextMessage
import com.artkachenko.livetextfinder.ui.FinderPrimaryAction
import com.artkachenko.livetextfinder.ui.FinderSearchLists
import com.artkachenko.livetextfinder.ui.FinderSecondaryAction
import com.artkachenko.livetextfinder.ui.FinderSpacing
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.search_list_discard_confirm
import livetextfinder.shared.generated.resources.search_list_discard_keep_editing
import livetextfinder.shared.generated.resources.search_list_discard_message
import livetextfinder.shared.generated.resources.search_list_discard_title
import livetextfinder.shared.generated.resources.search_list_transfer_cancel
import livetextfinder.shared.generated.resources.search_lists_close
import livetextfinder.shared.generated.resources.search_lists_heading
import org.jetbrains.compose.resources.stringResource

/** Modal list management above the scanner, with guarded dismissal of unsaved changes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FinderListsSheet(
    sessionState: SearchSessionState,
    searchEditing: SavedSearchEditingState,
    transferOwner: SearchListTransferOwner,
    snackbarHostState: SnackbarHostState,
    messages: @Composable () -> Unit = { Unit },
) {
    val confirmTransition = remember(searchEditing) {
        { next: SheetValue ->
            if (next == SheetValue.Hidden && searchEditing.hasDirtyDraft) {
                searchEditing.requestDismiss()
                false
            } else {
                true
            }
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmTransition,
    )
    val closeFocusRequester = remember { FocusRequester() }
    val transferState = transferOwner.state.value
    val persisted = sessionState.persisted
    ModalBottomSheet(
        onDismissRequest = searchEditing::requestDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        BoxWithConstraints {
            val sheetContentHeight = if (maxHeight >= 260.dp) maxHeight - 40.dp else maxHeight
            Column(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = sheetContentHeight)
                    .padding(bottom = FinderSpacing.large),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = FinderSpacing.large),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    LaunchedEffect(Unit) { closeFocusRequester.requestFocus() }
                    Text(
                        stringResource(Res.string.search_lists_heading),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    FinderSecondaryAction(
                        label = stringResource(Res.string.search_lists_close),
                        onClick = searchEditing::requestDismiss,
                        modifier = Modifier.focusRequester(closeFocusRequester),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = FinderSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(FinderSpacing.small),
                    ) {
                        messages()
                        val transferMessage = transferState.message
                        if (transferState.busy && transferMessage != null) {
                            ContextMessage(
                                message = transferMessage,
                                actionLabel = stringResource(
                                    Res.string.search_list_transfer_cancel,
                                ),
                                onAction = transferOwner::cancel,
                            )
                        }
                    }
                    FinderSearchLists(
                        lists = persisted.lists,
                        activeListId = persisted.activeListId,
                        editor = searchEditing.editor,
                        canSave = sessionState.loaded && !sessionState.recoveryRequired,
                        onNew = searchEditing::startNewList,
                        onSelect = searchEditing::selectFromSheet,
                        onEdit = searchEditing::editList,
                        onDelete = searchEditing::requestDelete,
                        onNameChange = searchEditing::updateDraftName,
                        onTermsChange = searchEditing::updateDraftTerms,
                        onSave = searchEditing::saveDraft,
                        onCancel = searchEditing::cancelDraft,
                        transferState = transferState,
                        onImportList = transferOwner::importList,
                        onExportList = transferOwner::exportList,
                        showHeading = false,
                    )
                }
                SnackbarHost(snackbarHostState)
            }
        }
        if (searchEditing.isDiscardConfirmationOpen) {
            DiscardListChangesDialog(searchEditing)
        }
    }
}

@Composable
private fun DiscardListChangesDialog(searchEditing: SavedSearchEditingState) {
    AlertDialog(
        onDismissRequest = searchEditing::keepEditing,
        title = { Text(stringResource(Res.string.search_list_discard_title)) },
        text = { Text(stringResource(Res.string.search_list_discard_message)) },
        confirmButton = {
            FinderPrimaryAction(
                label = stringResource(Res.string.search_list_discard_confirm),
                enabled = true,
                destructive = true,
                onClick = searchEditing::confirmDiscard,
            )
        },
        dismissButton = {
            FinderSecondaryAction(
                label = stringResource(Res.string.search_list_discard_keep_editing),
                onClick = searchEditing::keepEditing,
            )
        },
    )
}
