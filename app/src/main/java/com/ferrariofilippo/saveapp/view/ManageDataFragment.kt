// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentManageDataBinding
import com.ferrariofilippo.saveapp.util.CloudStorageUtil
import com.ferrariofilippo.saveapp.util.ImportExportUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.viewmodels.ManageDataViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ManageDataFragment : Fragment() {
    private lateinit var viewModel: ManageDataViewModel

    private var _binding: FragmentManageDataBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageDataBinding
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
        viewModel = ViewModelProvider(this)[ManageDataViewModel::class.java]
    }

    // Methods
    private fun setupUI() {
        val app = requireActivity().application as SaveAppApplication
        val act = app.getCurrentActivity()!! as MainActivity

        binding.restoreBackupButton.setOnClickListener {
            activity?.lifecycleScope?.launch {
                startDbRestore()
            }
        }
        binding.createBackupButton.setOnClickListener {
            ManageDataViewModel.setAreBackupButtonsEnabled(false)
            activity?.lifecycleScope?.launch {
                CloudStorageUtil.authorize(app, true)
            }
        }

        setupTransactionsButtons(act)
        setupSubscriptionsButtons(act)
        setupBudgetsButtons(act)
    }

    private fun setupTransactionsButtons(act: MainActivity) {
        binding.templateTransactionsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_TRANSACTIONS_TEMPLATE, act)
        }
        binding.importTransactionsButton.setOnClickListener {
            ImportExportUtil.getFromFile(ImportExportUtil.OPEN_TRANSACTIONS_FILE, act)
        }
        binding.exportTransactionsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_TRANSACTIONS_FILE, act)
        }
    }

    private fun setupSubscriptionsButtons(act: MainActivity) {
        binding.templateSubscriptionsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_SUBSCRIPTIONS_TEMPLATE, act)
        }
        binding.importSubscriptionsButton.setOnClickListener {
            ImportExportUtil.getFromFile(ImportExportUtil.OPEN_SUBSCRIPTIONS_FILE, act)
        }
        binding.exportSubscriptionsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_SUBSCRIPTIONS_FILE, act)
        }
    }

    private fun setupBudgetsButtons(act: MainActivity) {
        binding.templateBudgetsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_BUDGETS_TEMPLATE, act)
        }
        binding.importBudgetsButton.setOnClickListener {
            ImportExportUtil.getFromFile(ImportExportUtil.OPEN_BUDGETS_FILE, act)
        }
        binding.exportBudgetsButton.setOnClickListener {
            ImportExportUtil.createExportFile(ImportExportUtil.CREATE_BUDGETS_FILE, act)
        }
    }

    private fun startDbRestore() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.restore_backup))
            .setMessage(
                String.format(
                    resources.getString(R.string.restore_backup_dialog_message),
                    SettingsUtil.lastBackupTimeStamp
                )
            )
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.dialog_continue)) { _, _ ->
                ManageDataViewModel.setAreBackupButtonsEnabled(false)
                CloudStorageUtil.authorize(
                    requireActivity().application as SaveAppApplication,
                    false
                )
            }
            .show()
    }
}
