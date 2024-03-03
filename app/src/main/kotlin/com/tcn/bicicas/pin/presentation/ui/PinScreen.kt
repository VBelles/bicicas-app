package com.tcn.bicicas.pin.presentation.ui

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tcn.bicicas.R
import com.tcn.bicicas.common.ui.ScrollableAlertDialog
import com.tcn.bicicas.pin.PinModule
import com.tcn.bicicas.pin.presentation.PinState
import com.tcn.bicicas.pin.presentation.PinViewModel
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@Composable
fun PinScreen(
    pinModule: PinModule,
    padding: PaddingValues
) {
    val viewModel: PinViewModel = viewModel { pinModule.pinViewModel }
    val state by viewModel.state.collectAsState()
    PinScreen(state, padding, viewModel::onLogout, viewModel::onLogin)
}

@Composable
fun PinScreen(
    state: PinState,
    padding: PaddingValues,
    onLogout: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    when (state) {
        PinState.Loading -> {}
        is PinState.LoggedIn -> PinContent(state, padding, onLogout)
        is PinState.LoggedOut -> PinWelcomeContent(state, padding, onLogin)
    }
}

@Composable
private fun PinWelcomeContent(
    state: PinState.LoggedOut,
    padding: PaddingValues,
    onLogin: (String, String) -> Unit
) {
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var warningDialogShownInSession by rememberSaveable { mutableStateOf(false) }
    var showLoginDialog by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = stringResource(R.string.pin_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.sizeIn(maxWidth = 600.dp),
            )
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = stringResource(R.string.pin_welcome_message),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.sizeIn(maxWidth = 600.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    if (!warningDialogShownInSession) {
                        showWarningDialog = true
                    } else {
                        showLoginDialog = true
                    }
                },
                content = { Text(stringResource(R.string.pin_login)) }
            )
            Spacer(modifier = Modifier.weight(1f))

        }
    }

    if (showWarningDialog) {
        ScrollableAlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text(stringResource(R.string.pin_welcome_title_warning)) },
            text = { Text(stringResource(R.string.pin_welcome_message_warning)) },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text(stringResource(R.string.popup_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showWarningDialog = false
                    warningDialogShownInSession = true
                    showLoginDialog = true
                }) {
                    Text(stringResource(R.string.pin_welcome_button_continue))
                }
            }
        )
    }

    if (showLoginDialog) {
        LoginDialog(
            loginError = state.loginError,
            isLoading = state.loading,
            onDismissRequest = { showLoginDialog = false },
            onLogin = onLogin,
        )
    }

}


@Composable
private fun PinContent(state: PinState.LoggedIn, padding: PaddingValues, onLogout: () -> Unit) {
    var displayLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val onLogoutClicked = { displayLogoutDialog = true }
    val remainSeconds by produceState(
        (state.pinResult.remainTime / 1000).toInt(),
        state.pinResult.remainTime
    ) {
        var goalTime = 0L
        while (isActive) {
            withFrameMillis { time ->
                if (goalTime < time) goalTime = time + state.pinResult.remainTime
                val remainTime = goalTime - time
                val remainSeconds = (remainTime / 1000f).roundToInt()
                if (remainSeconds != value) value = remainSeconds
            }
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            PortraitPinContent(state, remainSeconds, onLogoutClicked)
        } else {
            LandscapePinContent(state, remainSeconds, onLogoutClicked)
        }
    }

    if (displayLogoutDialog) {
        AlertDialog(
            onDismissRequest = { displayLogoutDialog = false },
            title = { Text(stringResource(R.string.pin_logout)) },
            text = { Text(stringResource(R.string.pin_logout_popup_text)) },
            confirmButton = { TextButton(onClick = onLogout) { Text(stringResource(R.string.popup_accept)) } },
            dismissButton = {
                TextButton(onClick = { displayLogoutDialog = false }) {
                    Text(
                        stringResource(R.string.popup_cancel)
                    )
                }
            },
        )
    }

}

@Composable
private fun PortraitPinContent(
    state: PinState.LoggedIn,
    remainSeconds: Int,
    onLogoutButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onLogoutButtonClicked,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(stringResource(R.string.pin_logout))
        }
        Spacer(Modifier.height(12.dp))
        UserText(state.pinResult.user)
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .wrapContentHeight()
                .aspectRatio(1f)
        ) {
            CountDownIndicator(progress = remainSeconds / 30f, stroke = 12.dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(0.8f)
            ) {
                Crossfade(remainSeconds.toString(), label = "remainSeconds") { state ->
                    AutoSizeText(state, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                PinText(state.pinResult.pin, state.pinResult.nextPin)
                Spacer(modifier = Modifier.weight(1f))

            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LandscapePinContent(
    state: PinState.LoggedIn,
    remainSeconds: Int,
    onLogoutButtonClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Logout button
        TextButton(
            onClick = onLogoutButtonClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text(stringResource(R.string.pin_logout))
        }

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
            Spacer(Modifier.weight(0.25f / 4f))
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(top = 48.dp)
            ) {
                UserText(state.pinResult.user, modifier = Modifier.align(Alignment.TopEnd))
            }

            Spacer(Modifier.weight(0.25f / 4f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.25f)
                    .fillMaxHeight(0.8f)
                    .aspectRatio(1f, true)
            ) {
                CountDownIndicator(
                    progress = remainSeconds / 30f,
                    stroke = 12.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Crossfade(remainSeconds.toString(), label = "remainSeconds") { state ->
                    Text(
                        text = state,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
            Spacer(Modifier.weight(0.25f / 4f))
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(top = 48.dp)
            ) {
                PinText(
                    state.pinResult.pin, state.pinResult.nextPin,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
            Spacer(Modifier.weight(0.25f / 4f))
        }
    }
}

@Composable
private fun UserText(userNumber: String?, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = stringResource(R.string.pin_label_user),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = userNumber ?: "",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp)
        )
    }
}

@Composable
private fun PinText(pin: String, nextPin: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.pin_label_pin), style = MaterialTheme.typography.titleMedium)
        Crossfade(pin, label = "pin") { state ->
            Text(
                text = state,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
            )
        }
        Row(Modifier.offset((-6).dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Next",
                Modifier
                    .alpha(0.5f)
                    .offset(y = 2.dp)
                    .rotate(225f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Crossfade(nextPin, label = "nextPin") { state ->
                Text(
                    text = state,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.alpha(0.5f),
                )
            }
        }
    }
}