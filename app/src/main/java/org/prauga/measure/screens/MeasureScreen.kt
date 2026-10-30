package org.prauga.measure.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.theme.PvotSpacing
import kotlinx.coroutines.delay
import org.prauga.measure.R
import org.prauga.measure.measure.MeasureCamera
import org.prauga.measure.measure.MeasureUnitSystem

/** What stands between the user and a working AR view. */
private sealed interface MeasureAccess {
    data object NeedsCamera : MeasureAccess
    data object Checking : MeasureAccess
    data object Unsupported : MeasureAccess
    data object SessionFailed : MeasureAccess
    data object Ready : MeasureAccess
}

@Composable
fun MeasureScreen(unitSystem: MeasureUnitSystem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraGranted = it
    }
    var availability by remember { mutableStateOf<ArCoreApk.Availability?>(null) }
    var sessionFailed by remember { mutableStateOf(false) }
    var checkAgain by remember { mutableStateOf(0) }

    // ARCore reports a transient state while it checks; ask again until it settles.
    LaunchedEffect(cameraGranted, checkAgain) {
        if (!cameraGranted) return@LaunchedEffect
        availability = null
        sessionFailed = false
        var result = ArCoreApk.getInstance().checkAvailability(context)
        while (result.isTransient) {
            delay(RECHECK_MILLIS)
            result = ArCoreApk.getInstance().checkAvailability(context)
        }
        availability = result
    }

    val access = when {
        !cameraGranted -> MeasureAccess.NeedsCamera
        availability == null -> MeasureAccess.Checking
        !availability!!.isSupported -> MeasureAccess.Unsupported
        sessionFailed -> MeasureAccess.SessionFailed
        else -> MeasureAccess.Ready
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (access) {
            MeasureAccess.NeedsCamera -> Column(
                modifier = Modifier.padding(PvotSpacing.L),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PvotSpacing.L)
            ) {
                Text(
                    text = stringResource(R.string.measure_camera_needed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { requestCamera.launch(Manifest.permission.CAMERA) }) {
                    Text(stringResource(R.string.measure_allow_camera))
                }
            }

            MeasureAccess.Checking -> PvotLoadingContent()

            MeasureAccess.Unsupported -> PvotErrorContent(
                title = stringResource(R.string.measure_ar_unsupported_title),
                message = stringResource(R.string.measure_ar_unsupported_message),
                onRetry = { checkAgain++ }
            )

            MeasureAccess.SessionFailed -> PvotErrorContent(
                title = stringResource(R.string.measure_ar_failed_title),
                message = stringResource(R.string.measure_ar_failed_message),
                onRetry = { checkAgain++ }
            )

            MeasureAccess.Ready -> MeasureCamera(
                unitSystem = unitSystem,
                onSessionFailure = { sessionFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** How long to wait between ARCore availability checks while it is still working it out. */
private const val RECHECK_MILLIS = 200L
