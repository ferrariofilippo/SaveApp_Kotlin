// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.PeriodicInterval
import com.ferrariofilippo.saveapp.util.CloudStorageUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ManageDataViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val _areBackupButtonsEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

        fun setAreBackupButtonsEnabled(value: Boolean) {
            _areBackupButtonsEnabled.value = value
        }
    }

    private val app = application as SaveAppApplication

    // Observers
    private val _periodicBackupsEnabledObserver = Observer<Boolean> {
        if (it != _periodicBackupEnabledOldValue) {
            _periodicBackupEnabledOldValue = it
            viewModelScope.launch {
                SettingsUtil.setPeriodicBackupUpload(it)
                CloudStorageUtil.updateScheduledBackupUpload(app)
            }
        }
    }

    private val _requireWiFiObserver = Observer<Boolean> {
        if (it != _requireWifiOldValue) {
            _requireWifiOldValue = it
            viewModelScope.launch {
                SettingsUtil.setPeriodicBackupRequiresWiFi(it)
                CloudStorageUtil.updateScheduledBackupUpload(app)
            }
        }
    }

    // UI & Data
    private val _lastBackupTimeStampSource: Flow<String> = SettingsUtil.getLastBackupTimeStamp()
    private val _periodicBackupVisible: Flow<Boolean> = SettingsUtil.getPeriodicBackupVisible()

    private val _lastBackupTimeStamp: MutableLiveData<String> = MutableLiveData("")
    val lastBackupTimeStamp: LiveData<String> = _lastBackupTimeStamp

    private val _periodicBackupSectionVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    val periodicBackupSectionVisible: LiveData<Boolean> = _periodicBackupSectionVisible

    private var _periodicBackupEnabledOldValue = false
    var periodicBackupsEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    private var _requireWifiOldValue = true
    val requireWifi: MutableLiveData<Boolean> = MutableLiveData(true)

    private var _backupInterval = PeriodicInterval.WEEKLY
    val backupInterval get() = _backupInterval

    private var _integrityCheckInterval = PeriodicInterval.DAILY
    val integrityCheckInterval get() = _integrityCheckInterval

    val intervals = PeriodicInterval.entries.toTypedArray()

    val areBackupButtonsEnabled: LiveData<Boolean> = _areBackupButtonsEnabled

    init {
        runBlocking {
            _integrityCheckInterval =
                PeriodicInterval.from(SettingsUtil.getSummaryIntegrityCheckInterval().first())
            _backupInterval =
                PeriodicInterval.from(SettingsUtil.getPeriodicBackupInterval().first())
        }
        viewModelScope.launch {
            _periodicBackupEnabledOldValue = SettingsUtil.getPeriodicBackupUpload().first()
            periodicBackupsEnabled.value = _periodicBackupEnabledOldValue
            periodicBackupsEnabled.observeForever(_periodicBackupsEnabledObserver)
        }
        viewModelScope.launch {
            _requireWifiOldValue = SettingsUtil.getPeriodicBackupRequiresWiFi().first()
            requireWifi.value = _requireWifiOldValue
            requireWifi.observeForever(_requireWiFiObserver)
        }
        viewModelScope.launch {
            _periodicBackupVisible.collect {
                _periodicBackupSectionVisible.value = it
            }
        }
        viewModelScope.launch {
            _lastBackupTimeStampSource.collect {
                _lastBackupTimeStamp.value = String.format(app.getString(R.string.last_backup), it)
            }
        }
    }

    // Overrides
    override fun onCleared() {
        periodicBackupsEnabled.removeObserver(_periodicBackupsEnabledObserver)
        requireWifi.removeObserver(_requireWiFiObserver)
        super.onCleared()
    }

    // Methods
    fun setPeriodicBackupInterval(interval: PeriodicInterval) {
        if (interval != _backupInterval) {
            _backupInterval = interval
            viewModelScope.launch {
                SettingsUtil.setPeriodicBackupInterval(interval.minutes)
                CloudStorageUtil.updateScheduledBackupUpload(app)
            }
        }
    }

    fun setIntegrityCheckInterval(interval: PeriodicInterval) {
        if (interval != _integrityCheckInterval) {
            _integrityCheckInterval = interval
            viewModelScope.launch {
                SettingsUtil.setSummaryIntegrityCheckInterval(interval.minutes)
                StatsUtil.updateIntegrityCheckInterval(app)
            }
        }
    }
}
