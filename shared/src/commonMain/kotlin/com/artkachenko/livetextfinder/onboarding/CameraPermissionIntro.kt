package com.artkachenko.livetextfinder.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artkachenko.livetextfinder.ui.FinderCameraOverlay
import com.artkachenko.livetextfinder.ui.FinderPrimaryAction
import com.artkachenko.livetextfinder.ui.FinderSpacing
import livetextfinder.shared.generated.resources.Res
import livetextfinder.shared.generated.resources.camera_allow
import livetextfinder.shared.generated.resources.camera_intro_body
import livetextfinder.shared.generated.resources.camera_intro_permission
import livetextfinder.shared.generated.resources.camera_intro_settings
import livetextfinder.shared.generated.resources.camera_intro_title
import livetextfinder.shared.generated.resources.camera_open_settings
import livetextfinder.shared.generated.resources.scanner_photo_camera
import livetextfinder.shared.generated.resources.spotword_mark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * First-run intro shown in the camera viewport until camera access is granted.
 *
 * It says what the app does and why it needs the camera, then offers the permission request,
 * or a route to app settings once the system no longer shows the request.
 */
@Composable
internal fun CameraPermissionIntro(
    settingsRequired: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .widthIn(max = INTRO_MAX_WIDTH)
                .padding(horizontal = FinderSpacing.extraLarge, vertical = FinderSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinderSpacing.medium),
        ) {
            Image(
                painter = painterResource(Res.drawable.spotword_mark),
                contentDescription = null,
                modifier = Modifier.size(MARK_SIZE),
            )
            Text(
                text = stringResource(Res.string.camera_intro_title),
                modifier = Modifier.semantics { heading() },
                color = FinderCameraOverlay.onViewport,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    if (settingsRequired) {
                        Res.string.camera_intro_settings
                    } else {
                        Res.string.camera_intro_body
                    },
                ),
                color = FinderCameraOverlay.onViewportVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            if (!settingsRequired) {
                Text(
                    text = stringResource(Res.string.camera_intro_permission),
                    color = FinderCameraOverlay.onViewportVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(FinderSpacing.small))
            FinderPrimaryAction(
                label = stringResource(
                    if (settingsRequired) {
                        Res.string.camera_open_settings
                    } else {
                        Res.string.camera_allow
                    },
                ),
                enabled = true,
                icon = if (settingsRequired) null else Res.drawable.scanner_photo_camera,
                onClick = onAction,
            )
        }
    }
}

private val INTRO_MAX_WIDTH = 400.dp
private val MARK_SIZE = 72.dp
