// Copyright (c) 2023 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val movementRepo = saveAppApplication.movementRepository

    val sortAscending: MutableLiveData<Boolean> = MutableLiveData(false)

    val isSearchHidden: MutableLiveData<Boolean> = MutableLiveData(false)

    val searchBarHint: MutableLiveData<String> =
        MutableLiveData(application.resources.getString(R.string.searchbar_hint))

    val searchQuery: MutableLiveData<String> = MutableLiveData("")

    val tagId: MutableLiveData<Int?> = MutableLiveData(0)

    val year: MutableLiveData<Int> = MutableLiveData(LocalDate.now().year)

    private val _movements: MutableLiveData<List<TaggedMovement>> = MutableLiveData(listOf())
    val movements: LiveData<List<TaggedMovement>> = _movements

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    val showEmptyMessage: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            _movements.value = movementRepo.getAllTaggedByYearSorted(year.value!!.toString())
            showEmptyMessage.value = _movements.value?.size == 0
            tags.value = saveAppApplication.tagRepository.allTags.first().toTypedArray()
        }

        year.observeForever {
            viewModelScope.launch {
                updateMovements()
            }
        }
    }

    suspend fun updateMovements() {
        _movements.value = movementRepo.getAllTaggedByYearSorted(year.value!!.toString())
        showEmptyMessage.value = _movements.value?.size == 0
    }
}
