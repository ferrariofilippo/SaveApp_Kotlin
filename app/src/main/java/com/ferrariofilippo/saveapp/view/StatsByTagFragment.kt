// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.R
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByTagBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.SpacingUtil
import com.ferrariofilippo.saveapp.view.adapters.TagsStatsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByTagViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsByTagFragment : Fragment() {
    private lateinit var viewModel: StatsByTagViewModel

    private val msDuration: Int = 500

    private var _binding: FragmentStatsByTagBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsByTagBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupGraph()
        setupRecyclerView()
        setupYearPicker()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StatsByTagViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupGraph() {
        binding.byTagTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setType(checkedId == binding.byTagExpensesButton.id)
            }
        }
        binding.byTagTypeToggle.check(binding.byTagExpensesButton.id)

        viewModel.onTransactionsChangeCallback = { updateGraph() }
        binding.tagsPieChart.description.text = ""
        binding.tagsPieChart.setUsePercentValues(true)
        binding.tagsPieChart.holeRadius = 60f
        binding.tagsPieChart.legend.isEnabled = false
        binding.tagsPieChart.setHoleColor(0x00FFFFFF)
    }

    private fun setupRecyclerView() {
        val adapter = TagsStatsAdapter(
            requireContext(),
            runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) },
            SpacingUtil.padding
        )

        binding.tagsRecyclerView.adapter = adapter
        binding.tagsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.tagSumItems.observe(viewLifecycleOwner, Observer { sums ->
            sums.let {
                adapter.submitList(sums)
            }
        })
    }

    private fun setupYearPicker() {
        val yearAutoComplete = binding.yearInput.editText as AutoCompleteTextView
        val adapter = ArrayAdapter(
            binding.yearInput.context,
            R.layout.support_simple_spinner_dropdown_item,
            viewModel.years
        )

        yearAutoComplete.setAdapter(adapter)
        yearAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selection = parent.adapter.getItem(position) as String
            viewModel.setYear(selection)
        }
        yearAutoComplete.setText(viewModel.year.value, false)
    }

    private fun updateGraph() {
        binding.tagsPieChart.data = PieData(viewModel.dataSet)
        binding.tagsPieChart.animateXY(msDuration, msDuration, Easing.Linear)
    }
}
