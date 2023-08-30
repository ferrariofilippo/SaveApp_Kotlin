package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByTagBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.TagsStatsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByTagViewModel
import com.github.mikephil.charting.data.PieData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsByTagFragment : Fragment() {
    private lateinit var viewModel: StatsByTagViewModel

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
        viewModel.onMovementsChangeCallback = { updateGraph() }
        binding.tagsPieChart.description.text = ""
        binding.tagsPieChart.setUsePercentValues(true)
        binding.tagsPieChart.holeRadius = 60f
    }

    private fun setupRecyclerView() {
        val adapter = TagsStatsAdapter(
            requireContext(),
            runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }
        )

        binding.tagsRecyclerView.adapter = adapter
        binding.tagsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.tagSumItems.observe(viewLifecycleOwner, Observer { sums ->
            sums.let {
                adapter.submitList(sums)
            }
        })
    }

    private fun updateGraph() {
        binding.tagsPieChart.data = PieData(viewModel.dataSet)
        binding.tagsPieChart.invalidate()
    }
}
