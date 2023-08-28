package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget

class BudgetsViewModel(application: Application): AndroidViewModel(application) {
    private val budgetRepository = (application as SaveAppApplication).budgetRepository

    val budgets: LiveData<List<TaggedBudget>> = budgetRepository.allBudgets.asLiveData()

    private val _pastSectionCollapsed = MutableLiveData(true)
    val pastSectionCollapsed: LiveData<Boolean> = _pastSectionCollapsed

    fun changePastSectionVisibility() {
        _pastSectionCollapsed.value = !_pastSectionCollapsed.value!!
    }
}
