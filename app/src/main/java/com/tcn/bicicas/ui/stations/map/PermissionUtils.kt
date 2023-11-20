package com.tcn.bicicas.ui.stations.map

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

class PermissionState(
    private val permission: String,
    private val launcher: ActivityResultLauncher<String>,
    val isGranted: State<Boolean>,
) {
    fun launchPermissionRequest() {
        launcher.launch(permission)
    }
}

@Composable
fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    val isPermissionGranted = {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    val grantedState = remember(permission) {
        mutableStateOf(isPermissionGranted())
    }
    val launcher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        grantedState.value = granted
    }
    LifecycleResumeEffect(permission) {
        grantedState.value = isPermissionGranted()
        onPauseOrDispose { }
    }
    return remember(permission, launcher) {
        PermissionState(permission, launcher, grantedState)
    }
}