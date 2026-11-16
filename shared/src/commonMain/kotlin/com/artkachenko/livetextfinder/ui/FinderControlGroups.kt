package com.artkachenko.livetextfinder.ui

import com.artkachenko.livetextfinder.search.SearchMatchMode

/** The query, active-list context and search actions shared by the dock and inline search bar. */
internal class FinderSearchBar(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val searchListsLabel: String,
    val onSearchLists: () -> Unit,
    val activeListName: String? = null,
    val activeListTermCount: Int = 0,
    val matchMode: SearchMatchMode = SearchMatchMode.Substring,
    val saving: Boolean = false,
    val onTerms: () -> Unit = {},
    val onUseManual: () -> Unit = {},
    val onResults: () -> Unit = {},
)
