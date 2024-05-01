// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.util.SettingsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ManageDataViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application

    private val _lastBackupTimeStampSource:
            Flow<String> = SettingsUtil.getLastBackupTimeStamp()

    private val _lastBackupTimeStamp: MutableLiveData<String> = MutableLiveData("")

    val lastBackupTimeStamp: LiveData<String> = _lastBackupTimeStamp

    companion object {
        private val _areBackupButtonsEnabled: MutableLiveData<Boolean> = MutableLiveData(true)
        
        fun setAreBackupButtonsEnabled(value: Boolean) {
            _areBackupButtonsEnabled.value = value
        }
    }

    val areBackupButtonsEnabled: LiveData<Boolean> = _areBackupButtonsEnabled

    init {
        viewModelScope.launch {
            _lastBackupTimeStampSource.collect {
                _lastBackupTimeStamp.value = String.format(app.getString(R.string.last_backup, it))
            }
        }
    }
}
