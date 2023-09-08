package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val movementRepo = saveAppApplication.movementRepository

    val sortAscending: MutableLiveData<Boolean> = MutableLiveData(false)

    val isSearchHidden: MutableLiveData<Boolean> = MutableLiveData(false)

    val searchBarHint: MutableLiveData<String> =
        MutableLiveData(application.resources.getString(R.string.searchbar_hint))

    val searchQuery: MutableLiveData<String> = MutableLiveData("")

    val tagId: MutableLiveData<Int?> = MutableLiveData(0)

    val allMovements: LiveData<List<TaggedMovement>> = movementRepo.allTaggedMovements.asLiveData()

    val tags: MutableLiveData<Array<Tag>> = MutableLiveData<Array<Tag>>()

    init {
        viewModelScope.launch {
            tags.value = saveAppApplication.tagRepository.allTags.first().toTypedArray()
        }
    }
}
