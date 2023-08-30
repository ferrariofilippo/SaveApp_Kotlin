package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.statsitems.TagMovementsSum
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class StatsByTagViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val INCOME_ID = 1
    }

    private val _app = application as SaveAppApplication

    private val _tags: LiveData<List<Tag>> = _app.tagRepository.allTags.asLiveData()
    private val _movements: LiveData<List<TaggedMovement>> =
        _app.movementRepository.allTaggedMovements.asLiveData()

    private val _tagSums: MutableMap<Int, Double> = mutableMapOf()

    private val _setLabel: String = application.getString(R.string.expenses_by_tag)

    private val _tagSumItems: MutableLiveData<List<TagMovementsSum>> = MutableLiveData(listOf())
    val tagSumItems get(): LiveData<List<TagMovementsSum>> = _tagSumItems

    var dataSet: PieDataSet = PieDataSet(listOf(), _setLabel)

    var onMovementsChangeCallback: () -> Unit = { }

    init {
        _tags.observeForever { tags ->
            tags.let {
                _tagSums.clear()
                tags.forEach {
                    if (it.id != INCOME_ID)
                        _tagSums[it.id] = 0.0
                }
            }
            _movements.value?.let { movements ->
                calculateSums(movements)
            }
            updateEntries()
        }

        _movements.observeForever { movements ->
            _tagSums.keys.forEach {
                _tagSums[it] = 0.0
            }
            movements?.let {
                calculateSums(movements)
            }
            updateEntries()
        }
    }

    // Methods
    private fun calculateSums(movements: List<TaggedMovement>) {
        movements.forEach {
            if (it.tagId != INCOME_ID)
                _tagSums[it.tagId] = (_tagSums[it.tagId] ?: 0.0) + it.amount
        }
    }

    private fun updateEntries() {
        var generalSum = 0.0
        _tagSums.values.forEach { generalSum += it }

        val entries: MutableList<PieEntry> = mutableListOf()
        val items: MutableList<TagMovementsSum> = mutableListOf()
        val colors: MutableList<Int> = mutableListOf()
        _tags.value?.forEach {
            val sum = _tagSums[it.id] ?: 0.0
            items.add(TagMovementsSum(it.id, it.name, it.color, sum))
            if (sum != 0.0) {
                colors.add(_app.getColor(it.color))
                entries.add(PieEntry((sum * 100.0 / generalSum).toFloat(), it.name))
            }
        }

        _tagSumItems.value = items.toList()

        dataSet = PieDataSet(entries, _setLabel)
        dataSet.colors = colors
        dataSet.valueFormatter = PercentFormatter()

        onMovementsChangeCallback()
    }
}
