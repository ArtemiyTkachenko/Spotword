package com.artkachenko.livetextfinder.ui

/**
 * The capture button's alternatives, in priority order: capture or resume, cancel a pending
 * capture, then a secondary source action. A null label hides that alternative.
 */
internal class FinderCaptureActions(
    val captureLabel: String?,
    val captureEnabled: Boolean,
    val onCapture: () -> Unit,
    val pendingCaptureLabel: String?,
    val onCancelCapture: () -> Unit,
    val secondaryActionLabel: String?,
    val onSecondaryAction: () -> Unit,
)
