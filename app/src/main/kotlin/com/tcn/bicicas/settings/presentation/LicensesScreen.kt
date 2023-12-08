package com.tcn.bicicas.settings.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.tcn.bicicas.R
import com.tcn.bicicas.common.ui.ScrollableAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBackClicked: () -> Unit) {
    BackHandler(onBack = onBackClicked)
    val resources = LocalContext.current.resources
    val libs = remember {
        val json = resources.openRawResource(R.raw.aboutlibraries).readBytes()
            .decodeToString()
        Libs.Builder().withJson(json).build()
    }
    var openedLibIndex by remember { mutableIntStateOf(-1) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LicensesTopAppBar(scrollBehavior, onBackClicked) },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { padding ->
        val contentPadding =
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues()

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = contentPadding
        ) {
            itemsIndexed(libs.libraries) { position, lib ->
                LicenseItem(lib) { openedLibIndex = position }
            }
        }
    }

    if (openedLibIndex in libs.libraries.indices) {
        val lib = libs.libraries[openedLibIndex]
        val licenseText = remember(openedLibIndex) {
            lib.licenses.joinToString("\n") { it.licenseContent.orEmpty() }
        }
        if (licenseText.isNotEmpty()) {
            ScrollableAlertDialog(
                onDismissRequest = { openedLibIndex = -1 },
                title = {},
                text = { Text(licenseText) },
                confirmButton = {
                    TextButton(
                        onClick = { openedLibIndex = -1 },
                        content = { Text(stringResource(R.string.popup_accept)) }
                    )
                },
            )
        }
    }
}

@Composable
private fun LicenseItem(
    lib: Library,
    onOpenLicense: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier
            .clickable {
                val license = lib.licenses.firstOrNull()
                when {
                    license?.licenseContent != null -> onOpenLicense()
                    !license?.url.isNullOrBlank() -> uriHandler.openUri(license?.url!!)
                    !lib.website.isNullOrBlank() -> uriHandler.openUri(lib.website!!)
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = lib.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = lib.artifactVersion.orEmpty())
        }
        Text(text = lib.uniqueId, style = MaterialTheme.typography.bodySmall)
        if (lib.developers.isNotEmpty()) {
            Text(lib.developers.joinToString { it.name.orEmpty() })
        }
        if (lib.licenses.isNotEmpty()) {
            Text(
                text = lib.licenses.joinToString { it.name },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onBackClicked: () -> Unit) {
    MediumTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBackClicked,
                content = { Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null) }
            )
        },
        title = { Text(text = stringResource(R.string.settings_item_licenses)) },
        scrollBehavior = scrollBehavior,
    )
}