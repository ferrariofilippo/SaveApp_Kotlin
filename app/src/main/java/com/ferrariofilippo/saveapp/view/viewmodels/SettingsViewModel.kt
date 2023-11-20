// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.values())

    val defaultCurrencyId get() : Flow<Int> = SettingsUtil.getCurrency()

    val isUpdatingCurrency: LiveData<Boolean> =
        ((application as SaveAppApplication).getCurrentActivity() as MainActivity).isUpdatingCurrencies
}
