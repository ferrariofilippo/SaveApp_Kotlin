// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag

class ManageTagsViewModel(application: Application) : AndroidViewModel(application) {
    private val tagRepository = (application as SaveAppApplication).tagRepository

    val tags: LiveData<List<Tag>> = tagRepository.allTags.asLiveData()
}
