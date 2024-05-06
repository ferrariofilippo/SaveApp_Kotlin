// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByNameBinding
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByNameViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.BarData

class StatsByNameFragment : Fragment() {
    private lateinit var viewModel: StatsByNameViewModel

    private val msDuration: Int = 500

    private var _binding: FragmentStatsByNameBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsByNameBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StatsByNameViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupUI() {
        binding.byNameTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setDisplayType(checkedId == binding.showSumsButton.id)
            }
        }
        binding.byNameTypeToggle.check(binding.showFrequenciesButton.id)

        viewModel.onStatsChangeCallback = { updateGraph() }

        binding.byNameStatsBarChart.description.text = ""
        binding.byNameStatsBarChart.legend.isEnabled = false
        binding.byNameStatsBarChart.axisRight.isEnabled = false
        binding.byNameStatsBarChart.xAxis.valueFormatter = viewModel.valueFormatter
        binding.byNameStatsBarChart.xAxis.setDrawGridLines(false)
        binding.byNameStatsBarChart.setFitBars(true)
        binding.byNameStatsBarChart.setDrawValueAboveBar(true)
        binding.byNameStatsBarChart.setDrawGridBackground(false)
        binding.byNameStatsBarChart.setTouchEnabled(false)

        binding.byNameSearchBar.editText?.setOnFocusChangeListener { _, b ->
            var hint = ""
            if (!b) {
                try {
                    hint = requireContext().resources.getString(R.string.searchbar_hint)
                } catch (e: Exception) {
                    LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "setOnFocusChangeListener")
                }
            }

            viewModel.setHint(hint)
        }
    }

    private fun updateGraph() {
        try {
            val ctx = requireContext()
            val darkModeFlags = ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val txtColor = ctx.getColor(
                if (darkModeFlags == Configuration.UI_MODE_NIGHT_YES) R.color.white
                else R.color.black
            )

            binding.byNameStatsBarChart.xAxis.textColor = txtColor
            binding.byNameStatsBarChart.axisLeft.textColor = txtColor
            binding.byNameStatsBarChart.data = BarData(viewModel.dataSet)
            binding.byNameStatsBarChart.animateXY(msDuration, msDuration, Easing.Linear)
        } catch (e: Exception) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "updateGraph")
        }
    }
}
