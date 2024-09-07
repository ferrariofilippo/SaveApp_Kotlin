// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.launch

object TagUtil {
    private var _tags: MutableList<Tag> = mutableListOf()
    private var _tagIdToRootId: MutableMap<Int, Int> = mutableMapOf()
    val incomeTagIds: MutableSet<Int> = mutableSetOf()

    fun updateAll(application: SaveAppApplication) {
        incomeTagIds.clear()
        _tags.clear()
        application.applicationScope.launch {
            application.tagRepository.allTags.collect {
                _tags.clear()
                _tags.addAll(it)
                _tagIdToRootId.clear()
                incomeTagIds.clear()
                it.forEach { tag ->
                    if (tag.isIncome) {
                        incomeTagIds.add(tag.id)
                    }
                }
            }
        }
    }

    fun getTagRootId(tagId: Int): Int {
        var id = _tagIdToRootId[tagId]
        if (id != null) {
            return id
        }

        for (t in _tags) {
            if (t.id == tagId) {
                id = if (t.rootTagId == 0) t.id else t.rootTagId
                _tagIdToRootId[tagId] = id
                return id
            }
        }

        return 0
    }

    fun removeAllChildrenTags(tags: MutableList<Tag>, target: Tag) {
        var i = 0
        while (i < tags.size) {
            if (tags[i].parentTagId == target.id) {
                val next = tags[i]
                tags.removeAt(i)
                removeAllChildrenTags(tags, next)
            } else {
                ++i
            }
        }
    }
}
