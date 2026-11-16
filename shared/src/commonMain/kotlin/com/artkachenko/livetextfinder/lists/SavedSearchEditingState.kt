package com.artkachenko.livetextfinder.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import com.artkachenko.livetextfinder.search.HighlightColor
import com.artkachenko.livetextfinder.search.SavedSearchList
import com.artkachenko.livetextfinder.search.SearchListEditorState
import com.artkachenko.livetextfinder.search.SearchMatchMode
import com.artkachenko.livetextfinder.search.SearchSessionController
import com.artkachenko.livetextfinder.search.SearchSessionError
import com.artkachenko.livetextfinder.search.dispatchSearchListSelection
import com.artkachenko.livetextfinder.ui.RetainedInspectionActionDispatcher

/**
 * The screen's editing state for saved searches: the list manager's visibility, the editor
 * draft, the pending delete confirmation, and every edit to the saved search session.
 *
 * Every change that could make a pending list import or export stale goes through
 * [markContextChanged], which notifies the transfer owner and advances [contextRevision].
 */
internal class SavedSearchEditingState(
    private val session: SearchSessionController,
    private val onTransferContextChanged: () -> Unit,
) {
    var isOpen by mutableStateOf(false)
        private set
    var editor by mutableStateOf(SearchListEditorState())
        private set
    var contextRevision by mutableLongStateOf(0L)
        private set
    var pendingDeleteId by mutableStateOf<String?>(null)
        private set

    private var pendingDiscard by mutableStateOf<DiscardAction?>(null)

    val isDiscardConfirmationOpen: Boolean
        get() = pendingDiscard != null

    val hasDirtyDraft: Boolean
        get() {
            if (!editor.isOpen) return false
            val saved = session.state.value.persisted.lists
                .firstOrNull { it.id == editor.editingId }
            return editor.name != (saved?.name ?: "") ||
                editor.terms != (saved?.terms?.joinToString("\n") ?: "")
        }

    /** Invalidates pending list transfers after a search, list or editor change. */
    fun markContextChanged() {
        onTransferContextChanged()
        contextRevision++
    }

    fun setManualQuery(query: String) {
        markContextChanged()
        session.setManualQuery(query)
    }

    fun setMatchMode(matchMode: SearchMatchMode) {
        markContextChanged()
        session.setMatchMode(matchMode)
    }

    fun setHighlightColor(color: HighlightColor) {
        markContextChanged()
        session.setHighlightColor(color)
    }

    /** Reloads saved searches after a failed load, or resets them once a reset is confirmed. */
    fun retrySavedSearches() {
        markContextChanged()
        session.retry()
    }

    /** Opens the manager or requests dismissal of the open sheet. */
    fun toggle() {
        if (isOpen) {
            requestDismiss()
        } else {
            changeSheetVisibility()
        }
    }

    /** Closes a clean sheet or asks before discarding its draft. */
    fun requestDismiss() {
        if (isOpen) {
            requestDiscard(DiscardAction.CloseSheet)
        }
    }

    /** Activates a valid row and returns to the scanner. */
    fun selectFromSheet(id: String) {
        if (isOpen && canSelectFromSheet(id)) {
            requestDiscard(DiscardAction.SelectList(id))
        }
    }

    fun keepEditing() {
        pendingDiscard = null
    }

    fun confirmDiscard() {
        val action = pendingDiscard ?: return
        pendingDiscard = null
        applyDiscardAction(action)
    }

    private fun requestDiscard(action: DiscardAction) {
        if (isDiscardConfirmationOpen) return
        if (hasDirtyDraft) {
            pendingDiscard = action
        } else {
            applyDiscardAction(action)
        }
    }

    private fun applyDiscardAction(action: DiscardAction) {
        when (action) {
            DiscardAction.CloseSheet -> closeSheet()
            DiscardAction.NewList -> {
                markContextChanged()
                editor = editor.startNew()
            }
            is DiscardAction.SelectList -> {
                if (canSelectFromSheet(action.id)) {
                    closeSheet()
                    session.selectList(action.id)
                }
            }
            is DiscardAction.EditList -> {
                val saved = session.state.value.persisted.lists
                    .firstOrNull { it.id == action.id }
                if (saved != null) {
                    markContextChanged()
                    editor = editor.edit(saved)
                }
            }
        }
    }

    private fun canSelectFromSheet(id: String): Boolean {
        val sessionState = session.state.value
        return sessionState.loaded && sessionState.persisted.lists.any { it.id == id }
    }

    private fun closeSheet() {
        changeSheetVisibility()
        pendingDeleteId = null
    }

    private fun changeSheetVisibility() {
        RetainedInspectionActionDispatcher.toggleLists(
            managerOpen = isOpen,
            onContextChanged = ::markContextChanged,
            setManagerOpen = { isOpen = it },
            collapseSearch = {},
            closeEditor = { editor = editor.close() },
        )
    }

    private fun closeEditor() {
        markContextChanged()
        editor = editor.close()
    }

    /** Shows an imported list as an unsaved draft; the transfer owner is the caller. */
    fun acceptImportedDraft(name: String, terms: List<String>) {
        contextRevision++
        editor = editor.importedDraft(name, terms)
        isOpen = true
    }

    fun startNewList() {
        requestDiscard(DiscardAction.NewList)
    }

    fun editList(list: SavedSearchList) {
        if (editor.editingId != list.id) {
            requestDiscard(DiscardAction.EditList(list.id))
        }
    }

    fun updateDraftName(name: String) {
        markContextChanged()
        editor = editor.copy(name = name)
    }

    fun updateDraftTerms(terms: String) {
        markContextChanged()
        editor = editor.copy(terms = terms)
    }

    /** Saves the draft; a validation error stays on the open editor. */
    fun saveDraft() {
        markContextChanged()
        val error = session.saveList(editor.editingId, editor.name, editor.terms)
        editor = if (error == null) editor.close() else editor.copy(error = error)
    }

    fun cancelDraft() {
        closeEditor()
    }

    /** Activates a saved list, or manual search for a null [id]. */
    fun selectList(id: String?) {
        dispatchSearchListSelection(session, id, ::markContextChanged)
    }

    fun requestDelete(list: SavedSearchList) {
        pendingDeleteId = list.id
    }

    fun dismissDelete() {
        pendingDeleteId = null
    }

    /** Deletes the list and closes the editor when it was editing that list. */
    fun confirmDelete(id: String) {
        markContextChanged()
        session.deleteList(id)
        if (editor.editingId == id) {
            editor = editor.close()
        }
        pendingDeleteId = null
    }

    private sealed interface DiscardAction {
        object CloseSheet : DiscardAction
        object NewList : DiscardAction
        data class SelectList(val id: String) : DiscardAction
        data class EditList(val id: String) : DiscardAction
    }

    companion object {
        /**
         * Saves the manager's visibility, editor draft and pending delete confirmation, so a
         * half-typed list survives Activity recreation. Restoring does not notify the transfer
         * owner; the next composition gives it the restored context.
         */
        fun saver(
            session: SearchSessionController,
            onTransferContextChanged: () -> Unit,
        ): Saver<SavedSearchEditingState, Any> = listSaver(
            save = { state ->
                listOf(
                    state.isOpen,
                    state.pendingDeleteId,
                    state.editor.isOpen,
                    state.editor.editingId,
                    state.editor.name,
                    state.editor.terms,
                    state.editor.error?.name,
                )
            },
            restore = { saved ->
                SavedSearchEditingState(session, onTransferContextChanged).apply {
                    isOpen = saved[0] as Boolean
                    pendingDeleteId = saved[1] as String?
                    editor = SearchListEditorState(
                        isOpen = saved[2] as Boolean,
                        editingId = saved[3] as String?,
                        name = saved[4] as String,
                        terms = saved[5] as String,
                        error = (saved[6] as String?)?.let(SearchSessionError::valueOf),
                    )
                }
            },
        )
    }
}
