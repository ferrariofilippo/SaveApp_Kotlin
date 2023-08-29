package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val currencies: MutableLiveData<Array<Currencies>> =
        MutableLiveData<Array<Currencies>>(Currencies.values())

    val defaultCurrencyId get() : Flow<Int> = SettingsUtil.getCurrency()

    // Methods
    fun setDefaultCurrency(value: Currencies) {
        viewModelScope.launch {
            if (value.id != defaultCurrencyId.first()) {
                SettingsUtil.setCurrency(value)
                // TODO Update database values to new currency
            }
        }
    }
}
