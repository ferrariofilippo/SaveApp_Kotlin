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
import com.ferrariofilippo.saveapp.databinding.FragmentNewMovementBinding
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.view.adapters.BudgetsDropdownAdapter
import com.ferrariofilippo.saveapp.view.adapters.TagsDropdownAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.NewMovementViewModel
import com.google.android.material.datepicker.MaterialDatePicker
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

        setupUI()

        return binding.root;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NewMovementViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        binding.amountInput.editText?.setOnFocusChangeListener { view, b ->
            viewModel.amountOnFocusChange(view, b)
        }

        binding.saveButton.setOnClickListener { viewModel.insert() }
        binding.cancelButton.setOnClickListener { (activity as MainActivity).goBack() }
        binding.dateInput.editText?.setOnClickListener { showDatePicker() }

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
            }
        })

        val budgetAutoComplete = binding.budgetInput.editText as AutoCompleteTextView
        viewModel.budgets.observe(viewLifecycleOwner, Observer {
            it?.let {
                val today = LocalDate.now()
                val adapter = BudgetsDropdownAdapter(
                    binding.budgetInput.context,
                    R.layout.budget_dropdown_item,
                    it.filter { budget ->
                        !budget.to.isBefore(today)
                    }.toTypedArray()
                )

                budgetAutoComplete.setAdapter(adapter)
                budgetAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as TaggedBudget
                    viewModel.setBudget(selection)
                }
            }
        })

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

        val renewalAutoComplete = binding.renewalInput.editText as AutoCompleteTextView
        viewModel.renewalTypes.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = ArrayAdapter<RenewalType>(
                    binding.renewalInput.context,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    it
                )

                renewalAutoComplete.setAdapter(adapter)
                renewalAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as RenewalType
                    viewModel.setRenewalType(selection)
                }
            }
        })

        viewModel.onAmountChanged = { manageAmountError() }
        viewModel.onDescriptionChanged = { manageDescriptionError() }
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
