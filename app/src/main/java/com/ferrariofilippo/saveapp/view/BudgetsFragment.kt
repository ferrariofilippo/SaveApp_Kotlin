package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.databinding.FragmentBudgetsBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.BudgetsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.BudgetsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class BudgetsFragment : Fragment() {
    private lateinit var viewModel: BudgetsViewModel

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetsBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupRecyclerViews()
        setupButtons()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BudgetsViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun setupRecyclerViews() {
        val today = LocalDate.now()
        val currency = Currencies.from(runBlocking { SettingsUtil.getCurrency().first() })
        val from = getString(R.string.from_display)
        val to = getString(R.string.to_display)

        val activeAdapter = BudgetsAdapter(currency, from, to)
        val pastAdapter = BudgetsAdapter(currency, from, to)

        binding.activeBudgetsRecyclerView.adapter = activeAdapter
        binding.activeBudgetsRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.pastBudgetsRecyclerView.adapter = pastAdapter
        binding.pastBudgetsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.budgets.observe(viewLifecycleOwner, Observer { budgets ->
            budgets.let {
                activeAdapter.submitList(budgets.filter {
                    !today.isAfter(it.to)
                })
                pastAdapter.submitList(budgets.filter {
                    it.to.isBefore(today)
                })
            }
        })
    }

    private fun setupButtons() {
        binding.addBudgetButton.setOnClickListener {
            (activity as MainActivity).goToNewBudget()
        }

        binding.pastBudgetsCollapseButton.setOnClickListener {
            viewModel.changePastSectionVisibility()
        }
    }
}
