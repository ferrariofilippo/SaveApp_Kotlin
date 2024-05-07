// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val transactionRepo = saveAppApplication.transactionRepository

    val sortAscending: MutableLiveData<Boolean> = MutableLiveData(false)

    val isSearchHidden: MutableLiveData<Boolean> = MutableLiveData(false)

    val searchBarHint: MutableLiveData<String> =
        MutableLiveData(application.resources.getString(R.string.searchbar_hint))

    val searchQuery: MutableLiveData<String> = MutableLiveData("")

    val tagId: MutableLiveData<Int?> = MutableLiveData(0)

    val year: MutableLiveData<Int> = MutableLiveData(LocalDate.now().year)

    private val _transactions: MutableLiveData<List<TaggedTransaction>> = MutableLiveData(listOf())
    val transactions: LiveData<List<TaggedTransaction>> = _transactions

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    val showEmptyMessage: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            _transactions.value = transactionRepo.getAllTaggedByYearSorted(year.value!!.toString())
            showEmptyMessage.value = _transactions.value?.size == 0
            tags.value = saveAppApplication.tagRepository.allTags.first().toTypedArray()
        }

        year.observeForever {
            viewModelScope.launch {
                updateTransactions()
            }
        }
    }

    suspend fun updateTransactions() {
        _transactions.value = transactionRepo.getAllTaggedByYearSorted(year.value!!.toString())
        showEmptyMessage.value = _transactions.value?.size == 0
    }
}
