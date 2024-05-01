// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentSettingsBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.view.viewmodels.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SettingsFragment : Fragment() {
    private lateinit var viewModel: SettingsViewModel

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding
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
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }

    // Methods
    private fun setupUI() {
        setupCurrencyPicker()
        setupButtons()
    }

    private fun setupCurrencyPicker() {
        val currencyAutoComplete = binding.defaultCurrencyPicker.editText as AutoCompleteTextView
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = ArrayAdapter(
                    binding.defaultCurrencyPicker.context,
                    R.layout.support_simple_spinner_dropdown_item,
                    it
                )

                currencyAutoComplete.setAdapter(adapter)
                currencyAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val activity = requireActivity()
                    MaterialAlertDialogBuilder(activity)
                        .setIcon(com.ferrariofilippo.saveapp.R.drawable.baseline_warning_amber_24)
                        .setTitle(activity.getString(com.ferrariofilippo.saveapp.R.string.warning))
                        .setMessage(activity.getString(com.ferrariofilippo.saveapp.R.string.change_default_currency_alert_message))
                        .setNegativeButton(activity.getString(com.ferrariofilippo.saveapp.R.string.cancel)) { _, _ ->
                            val currency =
                                Currencies.from(runBlocking { viewModel.defaultCurrencyId.first() })

                            currencyAutoComplete.setText(currency.name, false)
                            currencyAutoComplete.clearFocus()
                        }
                        .setPositiveButton(activity.getString(com.ferrariofilippo.saveapp.R.string.dialog_continue)) { _, _ ->
                            val selection = parent.adapter.getItem(position) as Currencies
                            (requireActivity() as MainActivity).updateAllToNewCurrency(selection)
                        }
                        .show()
                }

                val currency = Currencies.from(runBlocking { viewModel.defaultCurrencyId.first() })
                currencyAutoComplete.setText(currency.name, false)
            }
        })
    }

    private fun setupButtons() {
        val app = requireActivity().application as SaveAppApplication
        val act = app.getCurrentActivity()!! as MainActivity

        binding.manageTagsButton.setOnClickListener { act.goToManageTags() }
        binding.manageDataButton.setOnClickListener { act.goToManageData() }
        binding.creditsButton.setOnClickListener {
            val uri = Uri.parse("https://github.com/ferrariofilippo/SaveApp_Kotlin")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.reportBugButton.setOnClickListener {
            val uri = Uri.parse("https://github.com/ferrariofilippo/SaveApp_Kotlin/issues/new")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
