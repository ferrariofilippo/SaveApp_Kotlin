package com.ferrariofilippo.saveapp.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.databinding.FragmentHistoryBinding
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.view.adapters.HistoryAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.HistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HistoryFragment : Fragment() {
    private lateinit var viewModel: HistoryViewModel

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        viewModel.allMovements.observe(viewLifecycleOwner, Observer { movements ->
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
                if (viewModel.allMovements.value != null)
                    setFilteredItems(adapter, viewModel.allMovements.value!!)
            }
        })

        binding.movementsRecyclerView.setOnTouchListener { _, _ -> onRecyclerClick() }
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
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setFilteredItems(adapter: HistoryAdapter, movements: List<TaggedMovement>) {
        if (viewModel.searchQuery.value!!.isBlank()) {
            adapter.submitList(
                if (viewModel.sortAscending.value!!) movements.reversed() else movements
            )
        } else {
            val query = viewModel.searchQuery.value!!.lowercase()
            adapter.submitList(
                (if (viewModel.sortAscending.value!!) movements.reversed() else movements).filter {
                    it.description.lowercase().contains(query)
                }
            )
        }
    }

    private fun onRecyclerClick(): Boolean {
        binding.searchBar.editText?.clearFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)

        return false
    }
}
