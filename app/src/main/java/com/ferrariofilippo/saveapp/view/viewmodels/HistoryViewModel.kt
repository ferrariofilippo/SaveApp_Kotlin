package com.ferrariofilippo.saveapp.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.data.repository.MovementRepository

class HistoryViewModel(private val movementRepository: MovementRepository) : ViewModel() {

}

sealed class HistoryViewModelFactory(private val movementRepository: MovementRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED CAST")
            return HistoryViewModel(movementRepository) as  T;
        }

        throw IllegalArgumentException("Unknown ViewModel class");
    }
}
