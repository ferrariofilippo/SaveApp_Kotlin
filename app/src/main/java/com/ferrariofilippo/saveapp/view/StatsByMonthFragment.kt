// Copyright (c) 2023 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByMonthBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.MonthsStatsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByMonthViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsByMonthFragment : Fragment() {
    private lateinit var viewModel: StatsByMonthViewModel

    private val msDuration: Int = 500

    private var _binding: FragmentStatsByMonthBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsByMonthBinding
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
        viewModel = ViewModelProvider(this)[StatsByMonthViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun setupGraph() {
        binding.byMonthTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setType(checkedId == binding.byMonthExpensesButton.id)
            }
        }
        binding. byMonthTypeToggle.check(binding.byMonthExpensesButton.id)

        viewModel.onMovementsChangeCallback = { updateGraph() }
        binding.monthsPieChart.description.text = ""
        binding.monthsPieChart.setUsePercentValues(true)
        binding.monthsPieChart.holeRadius = 60f
        binding.monthsPieChart.legend.isEnabled = false
        binding.monthsPieChart.setHoleColor(0x00FFFFFF)
    }

    private fun setupRecyclerView() {
        val adapter = MonthsStatsAdapter(
            runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }
        )

        binding.monthsRecyclerView.adapter = adapter
        binding.monthsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.monthSumItems.observe(viewLifecycleOwner, Observer { sums ->
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
        binding.monthsPieChart.data = PieData(viewModel.dataSet)
        binding.monthsPieChart.animateXY(msDuration, msDuration, Easing.Linear)
    }
}
