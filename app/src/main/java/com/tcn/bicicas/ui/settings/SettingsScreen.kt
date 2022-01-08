package com.tcn.bicicas.ui.settings

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.ui.settings.licenses.LicensesScreen
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import org.koin.androidx.compose.getViewModel


@Composable
fun SettingsScreen(onBackClicked: () -> Unit) {
    val viewModel: SettingsViewModel = getViewModel()
    val settings by viewModel.settingsState.collectAsState()
    SettingsScreen(
        settings = settings,
        onInitialScreenChanged = viewModel::onInitialScreenChanged,
        onThemeChanged = viewModel::onThemeChanged,
        onNavigationTypeChanged = viewModel::onNavigationTypeChanged,
        onDynamicColorEnabled = viewModel::onDynamicColorEnabled,
        onBackClicked = onBackClicked,
    )
}

@Composable
private fun SettingsScreen(
    settings: Settings,
    onInitialScreenChanged: (Int) -> Unit,
    onThemeChanged: (Int) -> Unit,
    onNavigationTypeChanged: (Int) -> Unit,
    onDynamicColorEnabled: (Boolean) -> Unit,
    onBackClicked: () -> Unit
) {
    BackHandler(onBack = onBackClicked)
    val scrollState = rememberScrollState()
    var licensesOpened by rememberSaveable { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            SettingsTopAppBar(onBackClicked, scrollState.value > 10)
            SettingsList(
                settings = settings,
                scrollState = scrollState,
                onInitialScreenChanged = onInitialScreenChanged,
                onThemeChanged = onThemeChanged,
                onNavigationTypeChanged = onNavigationTypeChanged,
                onDynamicColorEnabled = onDynamicColorEnabled,
                onLicensesClicked = { licensesOpened = true }
            )
        }
        AnimatedVisibility(
            visible = licensesOpened,
            enter = slideInVertically(tween()) { it },
            exit = slideOutVertically(tween()) { it },
        ) {
            LicensesScreen { licensesOpened = false }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SettingsList(
    settings: Settings,
    scrollState: ScrollState,
    onInitialScreenChanged: (Int) -> Unit,
    onThemeChanged: (Int) -> Unit,
    onNavigationTypeChanged: (Int) -> Unit,
    onDynamicColorEnabled: (Boolean) -> Unit,
    onLicensesClicked: () -> Unit,
) {
    BoxWithConstraints {
        val maxHeight = maxHeight
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .systemBarsPadding(top = false, bottom = false)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            ThemeSection(settings, onThemeChanged, onDynamicColorEnabled)

            Divider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(18.dp))
            NavigationSection(settings, onInitialScreenChanged, onNavigationTypeChanged)

            Divider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(18.dp))
            CreditsSection(onLicensesClicked)

            // Footer is placed filling the remaining space between the last item and the bottom
            // visible area with at least 24.dp of space

            val density = LocalDensity.current
            var lastItemY by remember { mutableStateOf(0.dp) }
            var creditsHeight by remember { mutableStateOf(0.dp) }

            Box(modifier = Modifier.onPlaced {
                lastItemY = with(density) { it.positionInParent().y.toDp() }
            })

            val bottomPadding = rememberInsetsPaddingValues(
                LocalWindowInsets.current.navigationBars, applyTop = false
            ).calculateBottomPadding() + 12.dp

            val remainSpace = maxHeight - lastItemY - creditsHeight - bottomPadding
            Spacer(modifier = Modifier.height((remainSpace).coerceAtLeast(18.dp)))

            println("LastItemY $lastItemY - creditsHeight $creditsHeight - maxHeight $maxHeight - bottomPadding $bottomPadding")


            Footer(Modifier
                .fillMaxWidth()
                .onSizeChanged { creditsHeight = with(density) { it.height.toDp() } }
                .padding(horizontal = 24.dp))

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun ThemeSection(
    settings: Settings,
    onThemeChanged: (Int) -> Unit,
    onDynamicColorEnabled: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.settings_title_theme),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
    SettingItemSelectBetween(
        title = stringResource(R.string.settings_item_theme),
        options = Settings.Theme.values().map { getThemeName(context, it) },
        selectedOption = settings.theme.ordinal,
        onOptionSelected = onThemeChanged,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SettingItemToggle(
            title = stringResource(R.string.settings_item_dynamic_color),
            subtitle = stringResource(R.string.settings_item_subtitle_dynamic_color),
            enabled = settings.dynamicColorEnabled,
            onToggle = onDynamicColorEnabled,
        )
    }
}

@Composable
fun NavigationSection(
    settings: Settings,
    onInitialScreenChanged: (Int) -> Unit,
    onNavigationTypeChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.settings_title_navigation),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp)
    )

    SettingItemSelectBetween(
        title = stringResource(R.string.settings_item_initial_screen),
        options = Settings.Screen.values().map { getScreenName(context, it) },
        selectedOption = settings.initialScreen.ordinal,
        onOptionSelected = onInitialScreenChanged,
    )

    SettingItemSelectBetween(
        title = stringResource(R.string.settings_item_navigation_type),
        options = Settings.NavigationType.values()
            .map { getNavigationTypeName(context, it) },
        selectedOption = settings.navigationType.ordinal,
        onOptionSelected = onNavigationTypeChanged,
    )
}

@Composable
fun CreditsSection(onLicensesClicked: () -> Unit) {
    Text(
        text = stringResource(R.string.settings_title_credits),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
    SettingItem(
        title = stringResource(R.string.settings_item_licenses),
        subtitle = stringResource(R.string.settings_item_subtitle_licenses),
        onClick = onLicensesClicked
    )
    SettingItem(
        title = stringResource(R.string.settings_item_version),
        subtitle = "${stringResource(R.string.app_name)} ${BuildConfig.VERSION_NAME}",
        onClick = {}
    )
}

@Composable
fun Footer(modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.app_name) + " " + BuildConfig.VERSION_NAME
                    + " " + stringResource(R.string.settings_item_credits_author),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        val uriHandler = LocalUriHandler.current
        val url = stringResource(R.string.settings_item_credits_url)
        Text(
            text = url,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { runCatching { uriHandler.openUri(url) } }
        )
    }
}

private fun getScreenName(context: Context, screen: Settings.Screen) = when (screen) {
    Settings.Screen.Pin -> context.getString(R.string.settings_option_pin)
    Settings.Screen.Stations -> context.getString(R.string.settings_option_stations)
    Settings.Screen.Map -> context.getString(R.string.settings_option_map)
}

private fun getThemeName(context: Context, theme: Settings.Theme) = when (theme) {
    Settings.Theme.System -> context.getString(R.string.settings_option_system)
    Settings.Theme.Light -> context.getString(R.string.settings_option_light)
    Settings.Theme.Dark -> context.getString(R.string.settings_option_dark)
}

private fun getNavigationTypeName(context: Context, navigationType: Settings.NavigationType) =
    when (navigationType) {
        Settings.NavigationType.BottomBar -> context.getString(R.string.settings_option_bottom_bar)
        Settings.NavigationType.Tabs -> context.getString(R.string.settings_option_tabs)
    }

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    endIcon: @Composable RowScope.() -> Unit = {}
) {
    Surface(onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(18.dp))
            endIcon()
        }
    }
}

@Composable
private fun SettingItemSelectBetween(
    title: String,
    options: List<String>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    SettingItem(title, options[selectedOption], { showDialog = true })
    if (showDialog) {
        AlertDialog(
            tonalElevation = 0.dp,
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            title = { Text(title) },
            text = {
                Column {
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    onOptionSelected(index)
                                }
                                .padding(vertical = 4.dp)
                                .selectableGroup(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = index == selectedOption,
                                onClick = {
                                    showDialog = false
                                    onOptionSelected(index)
                                })
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        )
    }
}


@Composable
private fun SettingItemToggle(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingItem(title, subtitle, { onToggle(!enabled) }) {
        Switch(
            checked = enabled, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}


@Composable
private fun SettingsTopAppBar(onBackClicked: () -> Unit, elevated: Boolean) {
    val elevation by animateDpAsState(targetValue = if (elevated) BarTonalElevation else 0.dp)
    Surface(tonalElevation = elevation) {
        Box(
            modifier = Modifier
                .systemBarsPadding(bottom = false)
                .height(BarHeight)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}