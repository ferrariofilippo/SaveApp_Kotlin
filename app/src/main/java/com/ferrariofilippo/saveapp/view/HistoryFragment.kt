package com.ferrariofilippo.saveapp.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
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
import com.ferrariofilippo.saveapp.databinding.FragmentHistoryBinding
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.ferrariofilippo.saveapp.util.BudgetUtil
import com.ferrariofilippo.saveapp.util.RecyclerEditAndDeleteGestures
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.view.adapters.HistoryAdapter
import com.ferrariofilippo.saveapp.view.adapters.TagsDropdownAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.HistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HistoryFragment : Fragment() {
    private lateinit var viewModel: HistoryViewModel

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupRecyclerView()
        setupBottomSheet()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Methods
    private fun setupRecyclerView() {
        val adapter =
            HistoryAdapter(Currencies.from(runBlocking { SettingsUtil.getCurrency().first() }))

        binding.movementsRecyclerView.adapter = adapter
        binding.movementsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.movements.observe(viewLifecycleOwner, Observer { movements ->
            movements?.let {
                setFilteredItems(adapter, it)
            }
        })

        binding.sortButton.setOnClickListener {
            binding.searchBar.editText?.clearFocus()
            viewModel.sortAscending.value = !viewModel.sortAscending.value!!
            adapter.submitList(adapter.currentList.reversed())
        }

        binding.searchBar.editText?.setOnFocusChangeListener { _, b ->
            viewModel.searchBarHint.value =
                if (b) "" else requireContext().resources.getString(R.string.searchbar_hint)
        }

        viewModel.searchQuery.observe(viewLifecycleOwner, Observer { query ->
            query?.let {
                if (viewModel.movements.value != null)
                    setFilteredItems(adapter, viewModel.movements.value!!)
            }
        })

        binding.movementsRecyclerView.setOnTouchListener { _, _ -> onRecyclerClick() }

        setupRecyclerGestures()
    }

    private fun setupRecyclerGestures() {
        val gestureCallback = object : RecyclerEditAndDeleteGestures(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.movementsRecyclerView.adapter as HistoryAdapter
                val movement = adapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT)
                    (activity as MainActivity).goToEditMovementOrSubscription(movement.id, true)
                else if (direction == ItemTouchHelper.LEFT)
                    onRemoveMovementInvoked(movement)
            }
        }

        val itemTouchHelper = ItemTouchHelper(gestureCallback)
        itemTouchHelper.attachToRecyclerView(binding.movementsRecyclerView)
    }

    private fun setupBottomSheet() {
        val bottomSheet = BottomSheetBehavior.from(binding.filtersBottomSheet)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                viewModel.isSearchHidden.value = newState == BottomSheetBehavior.STATE_HIDDEN
            }
        }
        bottomSheet.addBottomSheetCallback(bottomSheetCallback)

        binding.searchButton.setOnClickListener {
            binding.searchBar.editText?.clearFocus()
            bottomSheet.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        setupTagPicker()

        binding.decreaseYearButton.setOnClickListener {
            viewModel.year.value = (viewModel.year.value ?: LocalDate.now().year) - 1
        }
        binding.increaseYearButton.setOnClickListener {
            viewModel.year.value = (viewModel.year.value ?: LocalDate.now().year) + 1
        }
    }

    private fun setupTagPicker() {
        val tagAutoComplete = binding.tagFilterInput.editText as AutoCompleteTextView
        viewModel.tags.observe(viewLifecycleOwner, Observer {
            it?.let {
                val adapter = TagsDropdownAdapter(
                    binding.tagFilterInput.context,
                    R.layout.tag_dropdown_item,
                    it
                )

                tagAutoComplete.setAdapter(adapter)
                tagAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    val selection = parent.adapter.getItem(position) as Tag
                    viewModel.tagId.value = selection.id
                    setFilteredItems(
                        binding.movementsRecyclerView.adapter as HistoryAdapter,
                        viewModel.movements.value!!
                    )
                }
            }
        })

        binding.clearTagFilterButton.setOnClickListener {
            tagAutoComplete.text = null
            tagAutoComplete.clearFocus()
            viewModel.tagId.value = 0
            setFilteredItems(
                binding.movementsRecyclerView.adapter as HistoryAdapter,
                viewModel.movements.value!!
            )
        }
    }

    private fun setFilteredItems(adapter: HistoryAdapter, movements: List<TaggedMovement>) {
        val isFilteringByTag = viewModel.tagId.value != 0

        val values = if (viewModel.searchQuery.value!!.isNotBlank() && isFilteringByTag) {
            val query = viewModel.searchQuery.value!!.lowercase()
            movements.filter {
                it.description.lowercase().contains(query) && it.tagId == viewModel.tagId.value
            }
        } else if (isFilteringByTag) {
            movements.filter {
                it.tagId == viewModel.tagId.value
            }
        } else if (viewModel.searchQuery.value!!.isNotBlank()) {
            val query = viewModel.searchQuery.value!!.lowercase()
            movements.filter {
                it.description.lowercase().contains(query)
            }
        } else {
            movements
        }

        adapter.submitList(if (viewModel.sortAscending.value!!) values.reversed() else values)
    }

    private fun onRecyclerClick(): Boolean {
        binding.searchBar.editText?.clearFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)

        return false
    }

    private fun onRemoveMovementInvoked(taggedMovement: TaggedMovement) {
        val app = requireActivity().application as SaveAppApplication
        val movement = Movement(
            taggedMovement.id,
            taggedMovement.amount,
            taggedMovement.description,
            taggedMovement.date,
            taggedMovement.tagId,
            taggedMovement.budgetId
        )
        lifecycleScope.launch {
            app.movementRepository.delete(movement)
            BudgetUtil.removeMovementFromBudget(movement)
            viewModel.updateMovements()
            movement.amount *= -1
            StatsUtil.addMovementToStat(app, movement)
        }

        Snackbar.make(binding.coordinatorLayout, R.string.movement_deleted, Snackbar.LENGTH_SHORT)
            .setAction(R.string.undo) {
                lifecycleScope.launch {
                    movement.amount *= -1
                    BudgetUtil.tryAddMovementToBudget(movement)
                    app.movementRepository.insert(movement)
                    viewModel.updateMovements()
                    StatsUtil.addMovementToStat(app, movement)
                }
            }.show()
    }
}
