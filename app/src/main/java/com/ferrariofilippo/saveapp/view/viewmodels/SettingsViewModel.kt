// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.SaveAppThemes
import com.ferrariofilippo.saveapp.util.SettingsUtil
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _currenciesSectionCollapsed = MutableLiveData(true)
    val currenciesSectionCollapsed: LiveData<Boolean> = _currenciesSectionCollapsed

    private val _aboutSectionCollapsed = MutableLiveData(true)
    val aboutSectionCollapsed: LiveData<Boolean> = _aboutSectionCollapsed

    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.entries.toTypedArray())

    val defaultCurrencyId get(): Flow<Int> = SettingsUtil.getCurrency()

    val isUpdatingCurrency: LiveData<Boolean> =
        ((application as SaveAppApplication).getCurrentActivity() as MainActivity).isUpdatingCurrencies

    val defaultTheme get(): Flow<SaveAppThemes> = SettingsUtil.getTheme()

    val themesToStrings = mutableMapOf(
        SaveAppThemes.DefaultTheme to application.getString(R.string.default_theme),
        SaveAppThemes.DynamicColors to application.getString(R.string.dynamic_colors),
        SaveAppThemes.Dracula to application.getString(R.string.dracula_theme),
        SaveAppThemes.Nord to application.getString(R.string.nord_theme),
    )

    val stringToThemes = mutableMapOf(
        application.getString(R.string.default_theme) to SaveAppThemes.DefaultTheme,
        application.getString(R.string.dynamic_colors) to SaveAppThemes.DynamicColors,
        application.getString(R.string.dracula_theme) to SaveAppThemes.Dracula,
        application.getString(R.string.nord_theme) to SaveAppThemes.Nord,
    )

    fun toggleCurrenciesSectionVisibility() {
        _currenciesSectionCollapsed.value = !_currenciesSectionCollapsed.value!!
    }

    fun toggleAboutSectionVisibility() {
        _aboutSectionCollapsed.value = !_aboutSectionCollapsed.value!!
    }
}
