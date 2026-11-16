package com.artkachenko.livetextfinder.screen

/** The mutually exclusive arrangements of the main screen's bars, viewport and footer. */
internal enum class FinderLayoutMode {
    /** Live camera with the search and capture dock. */
    Live,

    /** Live camera with the scan-area draft actions in place of the dock. */
    AreaEditing,

    /** Frozen or imported image with inspection controls above the dock. */
    StillInspection,
    ;

    /** True when the live camera is the displayed source. */
    val isLiveCamera: Boolean get() = this == Live || this == AreaEditing
}

/** Resolves the layout mode; a retained still source outranks the area editor. */
internal fun finderLayoutMode(
    hasStillSource: Boolean,
    areaEditing: Boolean,
): FinderLayoutMode = when {
    hasStillSource -> FinderLayoutMode.StillInspection
    areaEditing -> FinderLayoutMode.AreaEditing
    else -> FinderLayoutMode.Live
}
