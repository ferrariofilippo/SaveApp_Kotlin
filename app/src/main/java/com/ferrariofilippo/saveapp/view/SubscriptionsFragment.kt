// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentSubscriptionsBinding
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import com.ferrariofilippo.saveapp.util.CustomRecyclerDecorator
import com.ferrariofilippo.saveapp.util.RecyclerEditAndDeleteGestures
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.SpacingUtil
import com.ferrariofilippo.saveapp.view.adapters.SubscriptionsAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.SubscriptionsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
        val adapter = SubscriptionsAdapter(requireContext(), currency, SpacingUtil.padding)

        binding.subscriptionsRecyclerView.adapter = adapter
        binding.subscriptionsRecyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.subscriptions.observe(viewLifecycleOwner, Observer { subscriptions ->
            subscriptions.let {
                adapter.submitList(subscriptions)
            }
        })

        setupRecyclerGestures()
        setupRecyclerDecorator()
    }

    private fun setupRecyclerGestures() {
        val gestureCallback = object : RecyclerEditAndDeleteGestures(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.subscriptionsRecyclerView.adapter as SubscriptionsAdapter
                val sub = adapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    (activity as MainActivity).goToEditTransactionOrSubscription(sub.id, false)
                } else if (direction == ItemTouchHelper.LEFT) {
                    onRemoveSubscriptionInvoked(sub)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(gestureCallback)
        itemTouchHelper.attachToRecyclerView(binding.subscriptionsRecyclerView)
    }

    private fun setupRecyclerDecorator() {
        binding.subscriptionsRecyclerView.addItemDecoration(
            CustomRecyclerDecorator(
                requireContext()
            )
        )
    }

    private fun onRemoveSubscriptionInvoked(taggedSubscription: TaggedSubscription) {
        val app = requireActivity().application as SaveAppApplication
        val subscription = Subscription(
            taggedSubscription.id,
            taggedSubscription.amount,
            taggedSubscription.description,
            taggedSubscription.renewalType,
            taggedSubscription.creationDate,
            taggedSubscription.lastPaid,
            taggedSubscription.nextRenewal,
            taggedSubscription.tagId,
            taggedSubscription.budgetId
        )
        lifecycleScope.launch {
            app.subscriptionRepository.delete(subscription)
        }

        Snackbar.make(
            binding.subscriptionsCoordinatorLayout,
            R.string.subscription_deleted,
            Snackbar.LENGTH_SHORT
        )
            .setAction(R.string.undo) {
                lifecycleScope.launch {
                    app.subscriptionRepository.insert(subscription)
                }
            }.show()
    }
}
