package com.artkachenko.livetextfinder.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.ScreenshotTier
import com.artkachenko.livetextfinder.ScreenshotVariant
import com.artkachenko.livetextfinder.assertScreenshot
import com.artkachenko.livetextfinder.drainMainLooper
import com.artkachenko.livetextfinder.ui.FinderCameraOverlay
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingScreenshotTest {
    @After
    fun tearDown() {
        drainMainLooper()
    }

    @Test
    fun permissionIntro() {
        for (variant in ScreenshotVariant.entries) {
            assertScreenshot(ScreenshotTier.REGION, "camera-permission-intro", "request", variant) {
                CameraPermissionIntro(
                    settingsRequired = false,
                    onAction = ::ignore,
                    modifier = Modifier.fillMaxSize()
                        .background(FinderCameraOverlay.viewportBackground),
                )
            }
        }
    }

    @Test
    fun permissionIntroAfterDenial() {
        assertScreenshot(
            ScreenshotTier.REGION,
            "camera-permission-intro",
            "settings",
            ScreenshotVariant.LIGHT,
        ) {
            CameraPermissionIntro(
                settingsRequired = true,
                onAction = ::ignore,
                modifier = Modifier.fillMaxSize()
                    .background(FinderCameraOverlay.viewportBackground),
            )
        }
    }

    @Test
    fun hintMessages() {
        for (hint in OnboardingHint.entries) {
            assertScreenshot(
                ScreenshotTier.ELEMENT,
                "onboarding-hint",
                hint.name.lowercase(),
                ScreenshotVariant.LIGHT,
            ) {
                Box(Modifier.background(HINT_BACKDROP).padding(8.dp)) {
                    OnboardingHintMessage(hint, onDismiss = ::ignore)
                }
            }
        }
    }

    private fun ignore() = Unit
}

private val HINT_BACKDROP = Color(0xFF30373C)
