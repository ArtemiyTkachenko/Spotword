package com.artkachenko.livetextfinder.screen

import kotlin.test.Test
import kotlin.test.assertEquals

class FinderLayoutModeTest {
    @Test
    fun stillSourceOutranksAreaEditor() {
        assertEquals(FinderLayoutMode.StillInspection, finderLayoutMode(true, false))
        assertEquals(FinderLayoutMode.StillInspection, finderLayoutMode(true, true))
    }

    @Test
    fun liveCameraUsesAreaEditorWhenOpen() {
        assertEquals(FinderLayoutMode.AreaEditing, finderLayoutMode(false, true))
        assertEquals(FinderLayoutMode.Live, finderLayoutMode(false, false))
    }

    @Test
    fun onlyLiveAndAreaEditingUseTheLiveViewport() {
        assertEquals(
            setOf(FinderLayoutMode.Live, FinderLayoutMode.AreaEditing),
            FinderLayoutMode.entries.filter { it.isLiveCamera }.toSet(),
        )
    }
}
