package com.artkachenko.livetextfinder

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.artkachenko.livetextfinder.search.inMemorySearchSessionController
import kotlinx.coroutines.runBlocking
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.appearance_dark
import livetextfinder.shared.generated.resources.appearance_title
import livetextfinder.shared.generated.resources.finder_options
import livetextfinder.shared.generated.resources.finder_options_help
import livetextfinder.shared.generated.resources.help_title
import livetextfinder.shared.generated.resources.inspection_lists
import livetextfinder.shared.generated.resources.scanner_camera_access_needed
import livetextfinder.shared.generated.resources.search_list_import
import livetextfinder.shared.generated.resources.search_list_more_actions
import livetextfinder.shared.generated.resources.search_list_name_label
import livetextfinder.shared.generated.resources.search_list_transfer_cancel
import livetextfinder.shared.generated.resources.search_lists_heading
import livetextfinder.shared.generated.resources.search_lists_new
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppScreenScreenshotTest {
    @Before
    fun denyCamera() {
        clearAppearance()
        shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA)
    }

    @After
    fun tearDown() {
        clearAppearance()
        drainMainLooper()
    }

    @Test
    fun permissionRequired() {
        for (variant in listOf(ScreenshotVariant.LIGHT, ScreenshotVariant.DARK)) {
            assertAppScreenshot(
                "camera-permission-required",
                variant,
                steps = listOf(expect(Res.string.scanner_camera_access_needed)),
            ) {
                App()
            }
        }
    }

    @Test
    fun listsSheet() = captureListsSheet("lists-sheet")

    @Test
    fun listsSheetEditor() = captureListsSheet(
        "lists-sheet-editor",
        click(Res.string.search_lists_new),
        expect(Res.string.search_list_name_label),
        largeText = true,
    )

    @Test
    fun listsSheetTransferInProgress() = captureListsSheet(
        "lists-sheet-transfer",
        click(Res.string.search_list_import),
        expect(Res.string.search_list_transfer_cancel),
    )

    private fun captureListsSheet(
        state: String,
        vararg actions: (SemanticsNode) -> Unit,
        largeText: Boolean = false,
    ) {
        val variants = mutableListOf(ScreenshotVariant.LIGHT, ScreenshotVariant.DARK)
        if (largeText) {
            variants.add(ScreenshotVariant.LIGHT_FONT_130)
        }
        for (variant in variants) {
            val session = inMemorySearchSessionController()
            check(session.saveList(null, "Invoices", "invoice") == null)
            session.selectList(null)
            assertAppScreenshot(
                state,
                variant,
                steps = listOf(
                    click(Res.string.inspection_lists),
                    expect(Res.string.search_lists_heading),
                    { root ->
                        val moreLabel = runBlocking {
                            getString(Res.string.search_list_more_actions, "Invoices")
                        }
                        val more = nodes(root).single { it.hasText(moreLabel) }
                        check(!more.config.contains(SemanticsProperties.Disabled))
                    },
                ) + actions,
                onCaptured = { activity ->
                    val shadow = shadowOf(activity)
                    val request = shadow.peekNextStartedActivityForResult()
                    if (request?.intent?.action == Intent.ACTION_OPEN_DOCUMENT) {
                        shadow.receiveResult(request.intent, Activity.RESULT_CANCELED, null)
                        drainMainLooper()
                    }
                },
            ) { App(session) }
        }
    }

    @Test
    fun optionsOpen() = capture(
        "options-open",
        click(Res.string.finder_options),
        expect(Res.string.finder_options_help),
    )

    @Test
    fun helpOpen() = capture(
        "help-open",
        click(Res.string.finder_options),
        ::expandOptions,
        click(Res.string.finder_options_help),
        expect(Res.string.help_title),
    )

    @Test
    fun appearanceChangesThemeAndPersistsChoice() {
        capture(
            "appearance-dark",
            click(Res.string.finder_options),
            click(Res.string.appearance_title),
            click(Res.string.appearance_dark),
            click(Res.string.finder_options),
            click(Res.string.appearance_title),
            { root ->
                val dark = nodes(root).single { it.hasText("Dark") }
                check(dark.config.getOrNull(SemanticsProperties.Selected) == true)
                val preferences = RuntimeEnvironment.getApplication()
                    .getSharedPreferences("appearance", 0)
                check(preferences.getString("theme", null) == "Dark")
            },
        )
    }

    private fun clearAppearance() {
        RuntimeEnvironment.getApplication().getSharedPreferences("appearance", 0)
            .edit().clear().commit()
    }

    private fun capture(state: String, vararg steps: (SemanticsNode) -> Unit) {
        assertAppScreenshot(state, ScreenshotVariant.LIGHT, steps.toList()) { App() }
    }

    private fun click(
        resource: StringResource,
        arguments: List<String> = emptyList(),
    ): (SemanticsNode) -> Unit = { root ->
        val label = runBlocking { getString(resource, *arguments.toTypedArray()) }
        val action = nodes(root).filter { it.hasText(label) }
            .firstNotNullOfOrNull { it.config.getOrNull(SemanticsActions.OnClick)?.action }
        checkNotNull(action) { "No screenshot action for '$label'" }
        check(action()) { "Screenshot action '$label' did not run" }
    }

    private fun expect(resource: StringResource): (SemanticsNode) -> Unit = { root ->
        val label = runBlocking { getString(resource) }
        check(nodes(root).any { it.hasText(label) }) { "Expected screenshot text '$label'" }
    }

    private fun expandOptions(root: SemanticsNode) {
        val expand = nodes(root).firstNotNullOfOrNull {
            it.config.getOrNull(SemanticsActions.Expand)?.action
        }
        if (expand != null) {
            check(expand())
        }
    }

    private fun SemanticsNode.hasText(label: String): Boolean =
        config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true ||
            config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true

    private fun nodes(root: SemanticsNode): Sequence<SemanticsNode> = sequence {
        yield(root)
        root.children.forEach { child -> yieldAll(nodes(child)) }
    }
}
