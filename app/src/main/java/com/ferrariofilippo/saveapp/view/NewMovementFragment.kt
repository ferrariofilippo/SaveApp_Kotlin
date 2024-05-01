// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentNewMovementBinding
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.view.adapters.BudgetsDropdownAdapter
import com.ferrariofilippo.saveapp.view.adapters.RenewalDropdownAdapter
import com.ferrariofilippo.saveapp.view.adapters.TagsDropdownAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.NewMovementViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NewMovementFragment : Fragment() {
    companion object {
        const val MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000
    }

    private lateinit var viewModel: NewMovementViewModel

    private var _binding: FragmentNewMovementBinding? = null

    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMovementBinding
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
        viewModel = ViewModelProvider(this)[NewMovementViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Methods
    private fun checkIfEditing() {
        val itemId = arguments?.getInt("itemId") ?: 0
        if (itemId == 0) {
            return
        }

        val application = requireActivity().application as SaveAppApplication
        val isMovement = arguments?.getBoolean("isMovement") ?: true

        viewModel.setIsSubscription(!isMovement)
        viewModel.setIsSubscriptionSwitchEnabled(false)

        if (isMovement) {
            val movement = runBlocking { application.movementRepository.getById(itemId) } ?: return

            viewModel.editingMovement = movement
            viewModel.setAmount(String.format("%.2f", movement.amount))
            viewModel.setDescription(movement.description)
            viewModel.setDate(movement.date)
        } else {
            val sub = runBlocking { application.subscriptionRepository.getById(itemId) } ?: return

            viewModel.editingSubscription = sub
            viewModel.setAmount(String.format("%.2f", sub.amount))
            viewModel.setDescription(sub.description)
            viewModel.setDate(sub.creationDate)
            viewModel.setRenewalType(sub.renewalType)
        }
    }

    private fun setupUI() {
        binding.amountInput.editText?.setOnFocusChangeListener { view, b ->
            viewModel.amountOnFocusChange(view, b)
        }

        binding.saveButton.setOnClickListener { viewModel.insert() }
        binding.cancelButton.setOnClickListener { (activity as MainActivity).popLastView() }
        binding.dateInput.editText?.setOnClickListener { showDatePicker() }
        binding.clearBudgetButton.setOnClickListener {
            binding.budgetInput.editText?.text = null
            binding.budgetInput.editText?.clearFocus()
            viewModel.setBudget(null)
        }

        setupTagPicker()
        setupBudgetPicker()
        setupCurrencyPicker()
        setupRenewalPicker()

        viewModel.onAmountChanged = { manageAmountError() }
        viewModel.onDescriptionChanged = { manageDescriptionError() }
        viewModel.validateTag = { manageTagError() }
    }

    private fun setupTagPicker() {
        val tagAutoComplete = binding.tagInput.editText as AutoCompleteTextView
        viewModel.tags.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = TagsDropdownAdapter(
                    binding.tagInput.context,
                    R.layout.tag_dropdown_item,
                    it
                )

                tagAutoComplete.setAdapter(adapter)
                tagAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as Tag
                    viewModel.setTag(selection)
                }

                val tagId =
                    if (viewModel.editingMovement != null) viewModel.editingMovement!!.tagId
                    else if (viewModel.editingSubscription != null) viewModel.editingSubscription!!.tagId
                    else 0

                if (tagId != 0) {
                    val i = it.indexOfFirst { tag -> tag.id == tagId }
                    tagAutoComplete.setText(it[i].name, false)
                    viewModel.setTag(it[i])
                }
            }
        })
    }

    private fun setupBudgetPicker() {
        val budgetAutoComplete = binding.budgetInput.editText as AutoCompleteTextView
        viewModel.budgets.observe(viewLifecycleOwner, Observer {
            it?.let {
                val today = LocalDate.now()
                val adapter = BudgetsDropdownAdapter(
                    binding.budgetInput.context,
                    R.layout.budget_dropdown_item,
                    it.filter { budget ->
                        budget.used < budget.max && !budget.to.isBefore(today)
                    }.toTypedArray()
                )

                budgetAutoComplete.setAdapter(adapter)
                budgetAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as TaggedBudget
                    viewModel.setBudget(selection)
                }

                val budgetId =
                    if (viewModel.editingMovement != null) viewModel.editingMovement!!.budgetId
                    else if (viewModel.editingSubscription != null) viewModel.editingSubscription!!.budgetId
                    else 0

                if (budgetId != 0) {
                    val i = it.indexOfFirst { budget -> budget.budgetId == budgetId }
                    budgetAutoComplete.setText(it[i].name, false)
                    viewModel.setBudget(it[i])
                }
            }
        })
    }

    private fun setupCurrencyPicker() {
        val currencyAutoComplete = binding.currencyInput.editText as AutoCompleteTextView
        viewModel.currencies.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = ArrayAdapter<Currencies>(
                    binding.currencyInput.context,
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

    private fun setupRenewalPicker() {
        val renewalAutoComplete = binding.renewalInput.editText as AutoCompleteTextView
        viewModel.renewalTypes.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = RenewalDropdownAdapter(
                    binding.renewalInput.context,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    it
                )

                renewalAutoComplete.setAdapter(adapter)
                renewalAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as RenewalType
                    viewModel.setRenewalType(selection)
                }

                renewalAutoComplete.setText(
                    adapter.getLocalizedName(viewModel.getRenewalType()),
                    false
                )
            }
        })
    }

    private fun manageAmountError() {
        val amount = viewModel.getAmount().replace(",", ".").toDoubleOrNull()

        binding.amountInput.error =
            if (amount != null && amount > 0.0) null
            else context?.resources?.getString(R.string.amount_error)
    }

    private fun manageDescriptionError() {
        binding.descriptionInput.error =
            if (viewModel.getDescription().isNotBlank()) null
            else context?.resources?.getString(R.string.description_error)
    }

    private fun manageTagError() {
        binding.tagInput.error =
            if (viewModel.getTag() != null) null
            else context?.resources?.getString(R.string.tag_error)
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(context?.resources?.getString(R.string.choose_date))
            .setSelection(viewModel.getDate().toEpochDay() * MILLISECONDS_IN_DAY)
            .build()

        datePicker.addOnPositiveButtonClickListener { setSelectedDate(datePicker) }
        datePicker.show(childFragmentManager, "movement_date_picker")
    }

    private fun setSelectedDate(datePicker: MaterialDatePicker<Long>) {
        datePicker.selection ?: return

        viewModel.setDate(
            Instant
                .ofEpochMilli(datePicker.selection!!)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        )
    }
}
