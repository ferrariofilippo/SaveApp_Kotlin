package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByMonthBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.MonthsStatsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByMonthViewModel
import com.github.mikephil.charting.data.PieData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsByMonthFragment : Fragment() {
    private lateinit var viewModel: StatsByMonthViewModel

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
        viewModel.onMovementsChangeCallback = { updateGraph() }
        binding.monthsPieChart.description.text = ""
        binding.monthsPieChart.setUsePercentValues(true)
        binding.monthsPieChart.holeRadius = 60f
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

    private fun updateGraph() {
        binding.monthsPieChart.data = PieData(viewModel.dataSet)
        binding.monthsPieChart.invalidate()
    }
}
