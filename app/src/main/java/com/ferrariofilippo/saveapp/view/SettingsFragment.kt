// Copyright (c) 2025 Filippo Ferrario
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
import androidx.lifecycle.lifecycleScope
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentSettingsBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.ThemeManagerUtil
import com.ferrariofilippo.saveapp.view.viewmodels.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.core.net.toUri

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
        setupThemePicker()
        setupButtons()

        binding.compactModeSwitch.isChecked =
            runBlocking { SettingsUtil.getUseCompactMode().first() }
        binding.compactModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { SettingsUtil.setUseCompactMode(isChecked) }
        }

        binding.enableFormulasSwitch.isChecked =
            runBlocking { SettingsUtil.getEnableFormulas().first() }
        binding.enableFormulasSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { SettingsUtil.setEnableFormulas(isChecked) }
        }
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

    private fun setupThemePicker() {
        val themeAutoComplete = binding.themePicker.editText as AutoCompleteTextView
        val adapter = ArrayAdapter(
            binding.themePicker.context,
            R.layout.support_simple_spinner_dropdown_item,
            viewModel.themesToStrings.values.toTypedArray()
        )

        themeAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val theme = viewModel.stringToThemes[parent.adapter.getItem(position) as String]
            if (theme != null) {
                runBlocking { ThemeManagerUtil.setTheme(theme) }
                MainActivity.requireRestart()
            }
        }

        themeAutoComplete.setAdapter(adapter)
        val theme = runBlocking { viewModel.defaultTheme.first() }
        themeAutoComplete.setText(viewModel.themesToStrings[theme], false)
    }

    private fun setupButtons() {
        val app = requireActivity().application as SaveAppApplication
        val act = app.getCurrentActivity()!! as MainActivity

        binding.manageTagsButton.setOnClickListener { act.goToManageTags() }
        binding.manageDataButton.setOnClickListener { act.goToManageData() }
        binding.creditsButton.setOnClickListener {
            val uri = "https://github.com/ferrariofilippo/SaveApp_Kotlin".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.reportBugButton.setOnClickListener {
            val uri = "https://github.com/ferrariofilippo/SaveApp_Kotlin/issues/new".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.clearLogsButton.setOnClickListener {
            LogUtil.clearLogs()
            Snackbar.make(
                act.findViewById(com.ferrariofilippo.saveapp.R.id.containerView),
                com.ferrariofilippo.saveapp.R.string.logs_cleared,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(act.findViewById(com.ferrariofilippo.saveapp.R.id.bottomAppBar)).show()
        }
        binding.exportLogsButton.setOnClickListener {
            act.exportLogFile.launch("saveapp.log")
        }
        binding.privacyButton.setOnClickListener {
            val uri = "https://saveapp.vercel.app/privacy".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.currenciesCollapseButton.setOnClickListener {
            viewModel.toggleCurrenciesSectionVisibility()
        }
        binding.aboutCollapseButton.setOnClickListener {
            viewModel.toggleAboutSectionVisibility()
        }
    }
}
