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
    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.entries.toTypedArray())

    val defaultCurrencyId get(): Flow<Int> = SettingsUtil.getCurrency()

    val isUpdatingCurrency: LiveData<Boolean> =
        ((application as SaveAppApplication).getCurrentActivity() as MainActivity).isUpdatingCurrencies

    val defaultTheme get(): Flow<SaveAppThemes> = SettingsUtil.getTheme()

    val themesToStrings = mutableMapOf(
        SaveAppThemes.DefaultTheme to application.getString(R.string.default_theme),
        SaveAppThemes.DynamicColors to application.getString(R.string.dynamic_colors),
    )

    val stringToThemes = mutableMapOf(
        application.getString(R.string.default_theme) to SaveAppThemes.DefaultTheme,
        application.getString(R.string.dynamic_colors) to SaveAppThemes.DynamicColors,
    )
}
