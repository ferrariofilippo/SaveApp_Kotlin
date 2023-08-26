package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val movementRepo = saveAppApplication.movementRepository

    val sortAscending: MutableLiveData<Boolean> = MutableLiveData(false)

    val isSearchHidden: MutableLiveData<Boolean> = MutableLiveData(false)

    val searchBarHint: MutableLiveData<String> =
        MutableLiveData(application.resources.getString(R.string.searchbar_hint))

    val searchQuery: MutableLiveData<String> = MutableLiveData("")

    val allMovements: LiveData<List<TaggedMovement>> = movementRepo.allTaggedMovements.asLiveData()
}
