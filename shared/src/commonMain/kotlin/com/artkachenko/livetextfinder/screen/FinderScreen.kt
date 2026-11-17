package com.artkachenko.livetextfinder.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.artkachenko.livetextfinder.AppAppearance
import com.artkachenko.livetextfinder.camera.FreezeMode
import com.artkachenko.livetextfinder.camera.ImageShareStatus
import com.artkachenko.livetextfinder.camera.LiveSearchAreaLifecycle
import com.artkachenko.livetextfinder.camera.allowsLiveCamera
import com.artkachenko.livetextfinder.camera.isImportInProgress
import com.artkachenko.livetextfinder.camera.liveSearchAreaAvailable
import com.artkachenko.livetextfinder.camera.rememberImageShareOwner
import com.artkachenko.livetextfinder.inspection.stillInspectionState
import com.artkachenko.livetextfinder.inspection.stillSourceOf
import com.artkachenko.livetextfinder.lists.SavedSearchEditingState
import com.artkachenko.livetextfinder.lists.SearchListTransferContextEffect
import com.artkachenko.livetextfinder.ocr.OcrUiState
import com.artkachenko.livetextfinder.ocr.isCameraRunning
import com.artkachenko.livetextfinder.onboarding.OnboardingHint
import com.artkachenko.livetextfinder.onboarding.OnboardingHintMessage
import com.artkachenko.livetextfinder.onboarding.rememberOnboardingHints
import com.artkachenko.livetextfinder.scanarea.LiveAreaBackHandler
import com.artkachenko.livetextfinder.scanarea.LiveSearchAreaState
import com.artkachenko.livetextfinder.scanarea.ScanAreaEditBar
import com.artkachenko.livetextfinder.search.SearchSessionController
import com.artkachenko.livetextfinder.search.rememberSearchListTransferOwner
import com.artkachenko.livetextfinder.status.FinderStatusRegion
import com.artkachenko.livetextfinder.ui.ContextMessage
import com.artkachenko.livetextfinder.ui.FinderSearchBar
import com.artkachenko.livetextfinder.ui.RetainedInspectionActionDispatcher
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.capture_cancel_import
import livetextfinder.shared.generated.resources.inspection_lists
import org.jetbrains.compose.resources.stringResource

/**
 * The scanner screen: the live preview or a retained image, the search dock, the saved-list
 * manager and every dialog.
 *
 * It creates the screen's state holders, derives what this composition displays and passes
 * both to the region composables. Displayed values are captured here; owners are reached
 * through [FinderSourceContext] where a callback must compare against current state.
 */
@Composable
internal fun FinderScreen(
    session: SearchSessionController,
    appearance: AppAppearance,
    onAppearanceChange: (AppAppearance) -> Unit,
) {
    var ocrState by remember { mutableStateOf(OcrUiState()) }
    val sessionState = session.state.value
    val activeSearch = sessionState.activeSearch
    val selectedList = sessionState.activeList
    val highlightColor = sessionState.persisted.highlightColor

    val transferOwner = rememberSearchListTransferOwner()
    val searchEditing = rememberSaveable(
        session,
        transferOwner,
        saver = SavedSearchEditingState.saver(session, transferOwner::contextChanged),
    ) {
        SavedSearchEditingState(session, transferOwner::contextChanged)
    }
    SearchListTransferContextEffect(transferOwner, session, sessionState, searchEditing)

    val liveArea = remember { LiveSearchAreaState() }
    LiveSearchAreaLifecycle(liveArea::reset)
    val viewportState = remember { mutableStateOf(IntSize.Zero) }
    val context = finderSourceContext(session, searchEditing, liveArea)
    val shareOwner = rememberImageShareOwner()
    val importedState = context.importedOwner.state.value
    val freezeState = context.frozenOwner.state.value
    val shouldShowViews = context.isContentVisible()

    val searchState = finderSearchState(
        sessionState = sessionState,
        ocrState = ocrState,
        importedState = importedState,
        freezeState = freezeState,
        liveArea = liveArea,
        viewport = viewportState.value,
        shouldShowViews = shouldShowViews,
    )
    val search = searchState.search
    val stillSource = stillSourceOf(importedState, freezeState)
    val sourceIdentity = stillSource?.identity

    val editorDraft = liveArea.editorDraft
    val areaEditorOpen = editorDraft != null
    val areaEditing = areaEditorOpen && stillSource == null &&
        freezeState.mode == FreezeMode.Live &&
        importedState.phase.allowsLiveCamera &&
        liveSearchAreaAvailable() && shouldShowViews
    val areaGestureEnabled = areaEditing &&
        viewportState.value.width > 0 && viewportState.value.height > 0 &&
        ocrState.status.isCameraRunning
    LaunchedEffect(areaEditorOpen, areaEditing) {
        if (areaEditorOpen && !areaEditing) {
            liveArea.cancelEditing()
        }
    }
    LiveAreaBackHandler(areaEditorOpen) {
        liveArea.cancelEditor(editorDraft?.token ?: 0L)
    }

    val inspection = stillInspectionState(
        source = stillSource,
        search = search,
        searchIdentity = activeSearch.identity,
        highlightColor = highlightColor,
        viewportState = viewportState,
        context = context,
    )
    LaunchedEffect(sourceIdentity, activeSearch.identity, freezeState.mode) {
        if (sourceIdentity == null) {
            inspection.clearSelection()
        }
    }
    LaunchedEffect(sourceIdentity, activeSearch.identity, highlightColor) {
        shareOwner.updateCurrent(sourceIdentity, activeSearch.identity, highlightColor)
    }

    val dialogs = rememberFinderDialogsState(activeSearch.identity)
    val actions = FinderSourceActions(
        context = context,
        liveArea = liveArea,
        shareOwner = shareOwner,
        importedState = importedState,
        freezeState = freezeState,
        ocrState = ocrState,
        search = search,
    )
    val searchBar = FinderSearchBar(
        query = sessionState.persisted.manualQuery,
        onQueryChange = searchEditing::setManualQuery,
        activeListName = selectedList?.name,
        activeListTermCount = selectedList?.terms?.size ?: 0,
        matchMode = activeSearch.matchMode,
        saving = sessionState.saving,
        searchListsLabel = stringResource(Res.string.inspection_lists),
        onSearchLists = searchEditing::toggle,
        onTerms = {
            RetainedInspectionActionDispatcher.showTerms(
                setTermsOpen = { dialogs.termResultsOpen = it },
                setOccurrencesOpen = { inspection.copySession.updateDialogOpen(it) },
            )
        },
        onUseManual = { searchEditing.selectList(null) },
        onResults = { dialogs.termResultsOpen = true },
    )
    val layoutMode = finderLayoutMode(stillSource != null, areaEditing)
    val hints = rememberOnboardingHints()
    val hasQuery = activeSearch.terms.any { term -> term.text.isNotBlank() }
    LaunchedEffect(hasQuery) {
        if (hasQuery) {
            hints.markSeen(OnboardingHint.Search)
        }
    }
    LaunchedEffect(searchEditing.isOpen) {
        if (searchEditing.isOpen) {
            hints.markSeen(OnboardingHint.Lists)
        }
    }
    val onboardingHint = hints.next(
        cameraRunning = layoutMode == FinderLayoutMode.Live && !searchEditing.isOpen &&
            ocrState.status.isCameraRunning,
        hasQuery = hasQuery,
        hasMatches = search.occurrences.isNotEmpty(),
    )

    val snackbarHostState = remember { SnackbarHostState() }
    FinderFeedbackEffects(
        hostState = snackbarHostState,
        copySession = inspection.copySession,
        shareState = shareOwner.state,
        transferState = transferOwner.state,
    )
    val floatingRetained = layoutMode == FinderLayoutMode.StillInspection
    val viewport: @Composable (Modifier) -> Unit = { modifier ->
        FinderViewport(
            inspection = inspection,
            viewportState = viewportState,
            searchState = searchState,
            ocrState = ocrState,
            onOcrState = { ocrState = it },
            importedState = importedState,
            freezeState = freezeState,
            liveArea = liveArea,
            context = context,
            areaEditing = areaEditing,
            areaGestureEnabled = areaGestureEnabled,
            modifier = modifier,
        )
    }
    Box(Modifier.fillMaxSize()) {
        if (!floatingRetained) {
            viewport(Modifier.fillMaxSize())
        }
        if (layoutMode != FinderLayoutMode.AreaEditing) {
            FinderStatusRegion(
                state = ocrState,
                importedState = importedState,
                freezeState = freezeState,
                sessionState = sessionState,
                search = search,
                presentation = searchState.presentation,
                highlights = searchState.highlights,
                resultsAvailable = searchState.resultsAvailable,
                areaSelected = liveArea.area != null,
                showSavedSearchMessages = !searchEditing.isOpen,
                onRetrySavedData = searchEditing::retrySavedSearches,
                onRequestReset = { dialogs.resetConfirmOpen = true },
            ) { label, messages ->
                FloatingLiveScanner(
                    searchBar = searchBar,
                    liveControls = context.liveCaptureControls,
                    captureEnabled = actions.captureEnabled &&
                        importedState.phase.allowsLiveCamera,
                    freezePending = freezeState.mode == FreezeMode.Freezing,
                    onFreeze = actions::capture,
                    onCancelFreeze = actions::cancelCapture,
                    areaEnabled = importedState.phase.allowsLiveCamera &&
                        freezeState.mode == FreezeMode.Live && liveSearchAreaAvailable(),
                    areaSelected = liveArea.area != null,
                    onArea = actions::startAreaEditing,
                    importEnabled = importedState.phase.allowsLiveCamera,
                    importPending = importedState.phase.isImportInProgress,
                    onImport = actions::beginImport,
                    onCancelImport = actions::leaveOrCancelImport,
                    onMatchMode = searchEditing::setMatchMode,
                    onOptions = { dialogs.optionsOpen = true },
                    statusLabel = label,
                    onStatus = {
                        hints.markSeen(OnboardingHint.Results)
                        searchBar.onTerms()
                    },
                    snackbarHostState = snackbarHostState.takeUnless { searchEditing.isOpen },
                    listsOpen = searchEditing.isOpen,
                    matchModeEnabled = sessionState.loaded && !sessionState.recoveryRequired,
                    retained = if (floatingRetained) {
                        floatingRetainedControls(
                            inspection,
                            searchState.resultsAvailable,
                            shareOwner.state.value.status == ImageShareStatus.Preparing,
                            actions,
                        )
                    } else {
                        null
                    },
                    retainedViewport = viewport,
                    messages = {
                        messages()
                        if (importedState.phase.isImportInProgress) {
                            ContextMessage(
                                message = label,
                                actionLabel = stringResource(Res.string.capture_cancel_import),
                                onAction = actions::leaveOrCancelImport,
                                floating = true,
                            )
                        }
                        if (onboardingHint != null) {
                            OnboardingHintMessage(
                                hint = onboardingHint,
                                onDismiss = { hints.markSeen(onboardingHint) },
                            )
                        }
                    },
                )
            }
        } else {
            Column(Modifier.align(Alignment.BottomCenter).safeDrawingPadding()) {
                SnackbarHost(snackbarHostState, Modifier.fillMaxWidth())
                ScanAreaEditBar(liveArea, editorDraft?.token ?: 0L, context, floating = true)
            }
        }
    }
    FinderDialogs(
        appearance = appearance,
        onAppearanceChange = onAppearanceChange,
        dialogs = dialogs,
        sessionState = sessionState,
        searchState = searchState,
        inspection = inspection,
        searchEditing = searchEditing,
        transferOwner = transferOwner,
        snackbarHostState = snackbarHostState,
        liveArea = liveArea,
        shouldShowViews = shouldShowViews,
    )
}
