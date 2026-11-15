package com.artkachenko.livetextfinder.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class RetainedInspectionActionDispatcherTest {
    private val calls = mutableListOf<String>()

    @Test
    fun toggleLists_opensAfterInvalidatingContextAndClosesEditor() {
        RetainedInspectionActionDispatcher.toggleLists(
            managerOpen = false,
            onContextChanged = { calls += "contextChanged" },
            setManagerOpen = { calls += "managerOpen=$it" },
            collapseSearch = { calls += "collapseSearch" },
            closeEditor = { calls += "closeEditor" },
        )

        assertEquals(listOf("contextChanged", "managerOpen=true", "closeEditor"), calls)
    }

    @Test
    fun toggleLists_closingAlsoCollapsesSearch() {
        RetainedInspectionActionDispatcher.toggleLists(
            managerOpen = true,
            onContextChanged = { calls += "contextChanged" },
            setManagerOpen = { calls += "managerOpen=$it" },
            collapseSearch = { calls += "collapseSearch" },
            closeEditor = { calls += "closeEditor" },
        )

        assertEquals(
            listOf("contextChanged", "managerOpen=false", "collapseSearch", "closeEditor"),
            calls,
        )
    }

    @Test
    fun showTerms_closesOccurrencesBeforeOpeningTerms() {
        RetainedInspectionActionDispatcher.showTerms(
            setTermsOpen = { calls += "terms=$it" },
            setOccurrencesOpen = { calls += "occurrences=$it" },
        )

        assertEquals(listOf("occurrences=false", "terms=true"), calls)
    }

    @Test
    fun showOccurrences_releasesInputBeforeSwappingDialogs() {
        RetainedInspectionActionDispatcher.showOccurrences(
            clearFocus = { calls += "clearFocus" },
            hideKeyboard = { calls += "hideKeyboard" },
            setTermsOpen = { calls += "terms=$it" },
            setOccurrencesOpen = { calls += "occurrences=$it" },
        )

        assertEquals(
            listOf("clearFocus", "hideKeyboard", "terms=false", "occurrences=true"),
            calls,
        )
    }

    @Test
    fun returnToSource_resetsScanAreaBeforeLeavingTheRetainedImage() {
        RetainedInspectionActionDispatcher.returnToSource(
            imported = true,
            resetLiveArea = { calls += "reset" },
            cancelImport = { calls += "cancelImport" },
            resumeFrozen = { calls += "resumeFrozen" },
        )
        RetainedInspectionActionDispatcher.returnToSource(
            imported = false,
            resetLiveArea = { calls += "reset" },
            cancelImport = { calls += "cancelImport" },
            resumeFrozen = { calls += "resumeFrozen" },
        )

        assertEquals(listOf("reset", "cancelImport", "reset", "resumeFrozen"), calls)
    }
}
