package com.artkachenko.livetextfinder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.ScreenshotTier
import com.artkachenko.livetextfinder.ScreenshotVariant
import com.artkachenko.livetextfinder.assertScreenshot
import com.artkachenko.livetextfinder.drainMainLooper
import com.artkachenko.livetextfinder.search.SavedSearchList
import com.artkachenko.livetextfinder.search.SearchListEditorState
import com.artkachenko.livetextfinder.search.SearchSessionError
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RegionScreenshotTest {
    private val receipts = SavedSearchList("receipts", "Receipts", listOf("invoice", "subtotal"))
    private val labels = SavedSearchList("labels", "Labels", listOf("lot number"))

    @After
    fun tearDown() {
        drainMainLooper()
    }

    @Test
    fun searchListsEmpty() = capture("finder-search-lists", "empty") {
        SearchLists()
    }

    @Test
    fun searchListsActiveList() = capture("finder-search-lists", "active-list") {
        SearchLists(lists = listOf(receipts, labels), activeListId = receipts.id)
    }

    @Test
    fun searchListsEditorError() = capture("finder-search-lists", "editor-error") {
        // The production manager scrolls. Show the validation message and save actions.
        Box(
            Modifier.height(ScreenshotTier.REGION.heightDp.dp)
                .verticalScroll(rememberScrollState(initial = Int.MAX_VALUE)),
        ) {
            SearchLists(
                editor = SearchListEditorState(
                    isOpen = true,
                    terms = "invoice",
                    error = SearchSessionError.ListNameBlank,
                ),
            )
        }
    }

    @Composable
    private fun SearchLists(
        lists: List<SavedSearchList> = emptyList(),
        activeListId: String? = null,
        editor: SearchListEditorState = SearchListEditorState(),
    ) {
        FinderSearchLists(
            lists = lists,
            activeListId = activeListId,
            editor = editor,
            canSave = true,
            onNew = ::ignore,
            onSelect = {},
            onEdit = {},
            onDelete = {},
            onNameChange = {},
            onTermsChange = {},
            onSave = ::ignore,
            onCancel = ::ignore,
        )
    }

    private fun capture(subject: String, state: String, content: @Composable () -> Unit) {
        assertScreenshot(ScreenshotTier.REGION, subject, state, ScreenshotVariant.LIGHT, content)
    }

    private fun ignore() = Unit
}
