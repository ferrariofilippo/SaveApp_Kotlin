// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.databinding.FragmentStatsByYearBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.YearStatsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.StatsByYearViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsByYearFragment : Fragment() {
    private lateinit var viewModel: StatsByYearViewModel

    private var _binding: FragmentStatsByYearBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsByYearBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupRecycler()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StatsByYearViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun setupRecycler() {
        val adapter = YearStatsAdapter(
            runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }
        )

        binding.yearsRecyclerView.adapter = adapter
        binding.yearsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.yearStatsItems.observe(viewLifecycleOwner, Observer { years ->
            adapter.submitList(years)
        })
    }
}
