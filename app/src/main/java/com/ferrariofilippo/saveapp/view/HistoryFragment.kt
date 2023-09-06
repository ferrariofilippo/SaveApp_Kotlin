package com.ferrariofilippo.saveapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.databinding.FragmentHistoryBinding
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.ferrariofilippo.saveapp.util.BudgetUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.view.adapters.HistoryAdapter
import com.ferrariofilippo.saveapp.view.viewmodels.HistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

        setupRecyclerGestures()
    }

    private fun setupRecyclerGestures() {
        val gestureCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            lateinit var deleteBackground: Drawable
            lateinit var deleteIcon: Drawable
            lateinit var editBackground: Drawable
            lateinit var editIcon: Drawable
            val iconMargin: Int = 16
            var initiated: Boolean = false

            private fun init() {
                val ctx = requireContext()
                deleteBackground = ColorDrawable(ContextCompat.getColor(ctx, R.color.red_200))
                deleteIcon = ContextCompat.getDrawable(ctx, R.drawable.baseline_delete_24)!!
                editBackground = ColorDrawable(ContextCompat.getColor(ctx, R.color.emerald_200))
                editIcon = ContextCompat.getDrawable(ctx, R.drawable.baseline_edit_24)!!
                initiated = true
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.movementsRecyclerView.adapter as HistoryAdapter
                val movement = adapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    // Open edit page
                } else if (direction == ItemTouchHelper.LEFT) {
                    onRemoveMovementInvoked(movement)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (viewHolder.adapterPosition == -1)
                    return

                if (!initiated)
                    init()

                if (dX != 0f) {
                    val background: Drawable
                    val icon: Drawable
                    val backgroundLeft: Int
                    val backgroundRight: Int
                    val iconLeft: Int
                    val iconRight: Int

                    if (dX > 0) {
                        background = editBackground
                        icon = editIcon
                        backgroundLeft = 0
                        backgroundRight = dX.toInt()
                        iconLeft = iconMargin
                        iconRight = iconMargin + icon.intrinsicWidth
                    } else {
                        background = deleteBackground
                        icon = deleteIcon
                        backgroundLeft = viewHolder.itemView.right + dX.toInt()
                        backgroundRight = viewHolder.itemView.right
                        iconLeft = viewHolder.itemView.right - iconMargin - icon.intrinsicWidth
                        iconRight = viewHolder.itemView.right - iconMargin
                    }

                    background.setBounds(
                        backgroundLeft,
                        viewHolder.itemView.top,
                        backgroundRight,
                        viewHolder.itemView.bottom
                    )
                    background.draw(c)

                    val height = viewHolder.itemView.bottom - viewHolder.itemView.top
                    val iconTop = viewHolder.itemView.top + (height - icon.intrinsicHeight) / 2

                    icon.setBounds(iconLeft, iconTop, iconRight, iconTop + icon.intrinsicHeight)
                    icon.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
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
            movement.amount *= -1
            StatsUtil.addMovementToStat(movement, taggedMovement.tagName)
        }

        Snackbar.make(binding.coordinatorLayout, R.string.movement_deleted, Snackbar.LENGTH_SHORT)
            .setAction(R.string.undo) {
                lifecycleScope.launch {
                    movement.amount *= -1
                    BudgetUtil.tryAddMovementToBudget(movement)
                    app.movementRepository.insert(movement)
                    StatsUtil.addMovementToStat(movement, taggedMovement.tagName)
                }
            }.show()
    }
}
