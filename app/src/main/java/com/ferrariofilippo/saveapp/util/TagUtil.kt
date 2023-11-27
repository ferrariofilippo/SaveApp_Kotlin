package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.SaveAppApplication
import kotlinx.coroutines.launch

object TagUtil {
    val incomeTagIds: MutableSet<Int> = mutableSetOf()

    fun updateAll(application: SaveAppApplication) {
        incomeTagIds.clear()
        application.applicationScope.launch {
            application.tagRepository.allTags.collect {
                it.filter { tag -> tag.isIncome }.forEach { tag -> incomeTagIds.add(tag.id) }
            }
        }
    }
}
