package com.tcn.bicicas.settings

import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.settings.domain.Settings
import com.tcn.bicicas.settings.presentation.SettingsViewModel


interface SettingsModule {
    val settingsViewModel: SettingsViewModel
}

class SettingsModuleImpl(
    private val storeManager: () -> StoreManager,
) : SettingsModule {
    override val settingsViewModel: SettingsViewModel
        get() = SettingsViewModel(storeManager().getStore(defaultValue = Settings()))
}