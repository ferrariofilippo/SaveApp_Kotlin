package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.databinding.FragmentSubscriptionsBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.SubscriptionsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.SubscriptionsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SubscriptionsFragment : Fragment() {
    private lateinit var viewModel: SubscriptionsViewModel

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        val currency = Currencies.from(runBlocking { SettingsUtil.getCurrency().first() })
        setupRecyclerView(currency)
        viewModel.setSymbol(currency.toSymbol())

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SubscriptionsViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun setupRecyclerView(currency: Currencies) {
        val adapter = SubscriptionsAdapter(currency)

        binding.subscriptionsRecyclerView.adapter = adapter
        binding.subscriptionsRecyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.subscriptions.observe(viewLifecycleOwner, Observer { subscriptions ->
            subscriptions.let {
                adapter.submitList(subscriptions)
            }
        })
    }
}
