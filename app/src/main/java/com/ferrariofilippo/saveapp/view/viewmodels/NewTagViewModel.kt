// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.util.TagUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NewTagViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    val defaultColor = application.getColor(R.color.emerald_700)

    val tags: MutableLiveData<List<Tag>> = MutableLiveData(listOf())

    val tagName: MutableLiveData<String> = MutableLiveData("")

    val tagColor: MutableLiveData<Int> = MutableLiveData(defaultColor)

    val isIncomeTag: MutableLiveData<Boolean> = MutableLiveData(false)

    val isIncomeTagSwitchEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    var oldTag: Tag? = null

    var parentTag: Tag? = null

    var onNameChanged: () -> Unit = { }

    init {
        viewModelScope.launch {
            @Suppress("UNNECESSARY_SAFE_CALL")
            tags.value = saveAppApplication.tagRepository.allTags
                .first()
                ?.onEach { TagUtil.computeTagFullName(it) }
                ?.sortedBy { it.fullName } ?: listOf()
        }
    }

    fun insert() = viewModelScope.launch {
        if (tagName.value != null && tagName.value!!.isNotBlank()) {
            val parentTagId = parentTag?.id ?: 0
            val rootTagId =
                if (parentTag == null) 0
                else if (parentTag?.rootTagId == 0) parentTag!!.id
                else parentTag!!.rootTagId

            val path =
                if (parentTagId == 0) ""
                else if (parentTag!!.path.isBlank()) parentTag!!.name
                else "${parentTag!!.path}/${parentTag!!.name}"

            val tag = Tag(
                oldTag?.id ?: 0,
                tagName.value!!,
                tagColor.value ?: defaultColor,
                isIncomeTag.value!!,
                parentTagId,
                rootTagId,
                path
            )

            if (tag.id == 0) {
                saveAppApplication.tagRepository.insert(tag)
                TagUtil.updateAll(saveAppApplication)
            } else {
                saveAppApplication.tagRepository.update(tag)
                if (oldTag != null && tag.parentTagId != oldTag!!.parentTagId) {
                    val copyOfTags = tags.value!!.toMutableList()
                    copyOfTags.remove(oldTag)
                    copyOfTags.add(tag)
                    updateChildren(copyOfTags, tag, if (rootTagId == 0) tag.id else rootTagId)
                }
            }

            val activity = saveAppApplication.getCurrentActivity() as MainActivity
            activity.popLastView()
            Snackbar.make(
                activity.findViewById(R.id.containerView),
                if (tag.id == 0) R.string.tag_created else R.string.tag_updated,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(activity.findViewById(R.id.bottomAppBar)).show()
        } else {
            onNameChanged()
        }
    }

    private suspend fun updateChildren(tagsList: MutableList<Tag>, parent: Tag, rootId: Int) {
        TagUtil.computeTagFullName(parent)
        var i = 0
        while (i < tagsList.size) {
            if (tagsList[i].parentTagId == parent.id) {
                val child = tagsList.removeAt(i)
                child.path = parent.fullName
                child.rootTagId = rootId
                saveAppApplication.tagRepository.update(child)
                updateChildren(tagsList, child, rootId)
            } else {
                ++i
            }
        }
    }
}
