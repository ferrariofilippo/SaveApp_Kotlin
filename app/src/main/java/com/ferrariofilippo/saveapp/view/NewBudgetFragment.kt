// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentNewBudgetBinding
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.view.adapters.TagsDropdownAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.NewBudgetViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId

class NewBudgetFragment : Fragment() {
    companion object {
        const val MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000
    }

    private lateinit var viewModel: NewBudgetViewModel

    private var _binding: FragmentNewBudgetBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewBudgetBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        checkIfEditing()
        setupUI()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NewBudgetViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Methods
    private fun checkIfEditing() {
        val itemId = arguments?.getInt("itemId") ?: 0
        if (itemId == 0) {
            return
        }

        val application = requireActivity().application as SaveAppApplication
        val budget = runBlocking { application.budgetRepository.getById(itemId) } ?: return

        viewModel.editingBudget = budget
        viewModel.setIsUsedInputVisible(true)
        viewModel.setAmount(String.format("%.2f", budget.max))
        viewModel.setUsed(String.format("%.2f", budget.used))
        viewModel.setName(budget.name)
        viewModel.setFromDate(budget.from)
        viewModel.setToDate(budget.to)
    }

    private fun setupUI() {
        binding.budgetAmountInput.editText?.setOnFocusChangeListener { view, b ->
            viewModel.amountOnFocusChange(view, b)
        }
        binding.budgetUsedInput.editText?.setOnFocusChangeListener { view, b ->
            viewModel.usedOnFocusChange(view, b)
        }

        binding.budgetSaveButton.setOnClickListener { viewModel.insert() }
        binding.budgetCancelButton.setOnClickListener { (activity as MainActivity).goBack() }

        binding.fromDateInput.editText?.setOnClickListener {
            showDatePicker(
                getString(R.string.initial_date),
                true
            )
        }
        binding.toDateInput.editText?.setOnClickListener {
            showDatePicker(
                getString(R.string.ending_date),
                false
            )
        }

        setupPickers()

        viewModel.onAmountChanged = { manageAmountError() }
        viewModel.onUsedChanged = { manageUsedError() }
        viewModel.onNameChanged = { manageNameError() }
        viewModel.onToDateChanged = { manageToDateError() }
    }

    private fun setupPickers() {
        val tagAutoComplete = binding.budgetTagInput.editText as AutoCompleteTextView
        viewModel.tags.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = TagsDropdownAdapter(
                    binding.budgetTagInput.context,
                    R.layout.tag_dropdown_item,
                    it
                )

                tagAutoComplete.setAdapter(adapter)
                tagAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as Tag
                    viewModel.setTag(selection)
                }

                if (viewModel.editingBudget != null && viewModel.editingBudget!!.tagId != 0) {
                    val i = it.indexOfFirst { tag -> tag.id == viewModel.editingBudget!!.tagId }
                    tagAutoComplete.setText(it[i].name, false)
                    viewModel.setTag(it[i])
                }
            }
        })

        val currencyAutoComplete = binding.budgetCurrencyInput.editText as AutoCompleteTextView
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = ArrayAdapter(
                    binding.budgetCurrencyInput.context,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    it
                )

                currencyAutoComplete.setAdapter(adapter)
                currencyAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as Currencies
                    viewModel.setCurrency(selection)
                }
                currencyAutoComplete.setText(viewModel.baseCurrency.name, false)
            }
        })
    }

    private fun manageAmountError() {
        val amount = viewModel.getAmount().replace(",", ".").toDoubleOrNull()

        binding.budgetAmountInput.error =
            if (amount != null && amount > 0.0) null
            else context?.resources?.getString(R.string.amount_error)
    }

    private fun manageUsedError() {
        val used = viewModel.getUsed().replace(",", ".").toDoubleOrNull()

        binding.budgetAmountInput.error =
            if (used != null && used >= 0.0) null
            else context?.resources?.getString(R.string.used_not_valid)
    }

    private fun manageNameError() {
        binding.budgetNameInput.error =
            if (viewModel.getName().isNotBlank()) null
            else context?.resources?.getString(R.string.name_error)
    }

    private fun manageToDateError() {
        binding.toDateInput.error =
            if (viewModel.getToDate().isAfter(viewModel.getFromDate())) null
            else context?.resources?.getString(R.string.to_date_not_valid_error)
    }

    private fun showDatePicker(title: String, isFromDate: Boolean) {
        val selectEpochDay =
            if (isFromDate) viewModel.getFromDate().toEpochDay()
            else viewModel.getToDate().toEpochDay()

        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(title)
            .setSelection(selectEpochDay * MILLISECONDS_IN_DAY)
            .build()

        datePicker.addOnPositiveButtonClickListener { setSelectedDate(datePicker, isFromDate) }
        datePicker.show(childFragmentManager, "movement_date_picker")
    }

    private fun setSelectedDate(datePicker: MaterialDatePicker<Long>, isFromDate: Boolean) {
        datePicker.selection ?: return

        val date = Instant
            .ofEpochMilli(datePicker.selection!!)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        if (isFromDate) {
            viewModel.setFromDate(date)
        } else {
            viewModel.setToDate(date)
        }
    }
}
