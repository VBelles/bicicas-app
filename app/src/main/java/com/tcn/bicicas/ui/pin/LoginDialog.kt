package com.tcn.bicicas.ui.pin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.tcn.bicicas.R
import com.tcn.bicicas.ui.components.outlinedTextFieldColorsMaterial3


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginDialog(
    loginError: PinState.LoginError?,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    AlertDialog(
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.pin_login)) },
        text = { LoginContent(loginError, isLoading, onLogin) },
        confirmButton = { },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(max = 500.dp)
            .fillMaxWidth(0.85f)
    )
}

@Composable
private fun LoginContent(
    loginError: PinState.LoginError?,
    isLoading: Boolean,
    onLoginClicked: (String, String) -> Unit
) {

    val window = (LocalView.current.parent as DialogWindowProvider).window
    LaunchedEffect(Unit) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {

        val (username, setUsername) = rememberSaveable { mutableStateOf("") }
        val (password, setPassword) = rememberSaveable { mutableStateOf("") }
        val loginEnabled = username.isNotBlank() && password.isNotBlank() && !isLoading

        UserInput(username, setUsername)

        PasswordInput(password, setPassword) {
            if (loginEnabled) onLoginClicked(username, password)
        }

        Text(
            text = when (loginError) {
                PinState.LoginError.WrongUserPass -> stringResource(R.string.pin_login_error_wrong_user_pass)
                PinState.LoginError.Network -> stringResource(R.string.pin_login_error_network)
                PinState.LoginError.Unknown -> stringResource(R.string.pin_login_error_unknown)
                null -> ""
            },
            textAlign = TextAlign.Center
        )

        OutlinedButton(
            enabled = loginEnabled,
            onClick = { onLoginClicked(username, password) },
            content = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                    Text(
                        stringResource(R.string.pin_login),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UserInput(value: String, onValueChanged: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(stringResource(R.string.pin_field_username)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrect = false,
            imeAction = ImeAction.Next
        ),
        colors = outlinedTextFieldColorsMaterial3(),
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                if (event.key.keyCode == Key.Tab.keyCode || event.key.keyCode == Key.Enter.keyCode) {
                    focusManager.moveFocus(FocusDirection.Next)
                    true
                } else {
                    false
                }
            },
    )
}

@Composable
fun PasswordInput(value: String, onValueChanged: (String) -> Unit, onAction: () -> Unit) {
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    val visualTransform =
        if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
    val iconButton =
        if (passwordVisibility) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff

    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(stringResource(R.string.pin_field_password)) },
        singleLine = true,
        visualTransformation = visualTransform,
        trailingIcon = {
            IconButton(
                onClick = { passwordVisibility = !passwordVisibility },
                content = { Icon(iconButton, null) },
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrect = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions { onAction() },
        colors = outlinedTextFieldColorsMaterial3(),
        modifier = Modifier.fillMaxWidth(),
    )
}