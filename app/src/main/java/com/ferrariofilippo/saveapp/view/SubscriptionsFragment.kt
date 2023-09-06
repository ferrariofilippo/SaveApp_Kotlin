package com.ferrariofilippo.saveapp.view

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ferrariofilippo.saveapp.databinding.FragmentSubscriptionsBinding
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import com.ferrariofilippo.saveapp.util.SettingsUtil
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
        val adapter = SubscriptionsAdapter(currency)

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
                val adapter = binding.subscriptionsRecyclerView.adapter as SubscriptionsAdapter
                val subscription = adapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT) {
                    // Open edit page
                } else if (direction == ItemTouchHelper.LEFT) {
                    onRemoveMovementInvoked(subscription)
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
        itemTouchHelper.attachToRecyclerView(binding.subscriptionsRecyclerView)
    }

    private fun setupRecyclerDecorator() {
        binding.subscriptionsRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            lateinit var background: Drawable
            var initiated: Boolean = false

            private fun init() {
                background =
                    ColorDrawable(ContextCompat.getColor(requireContext(), R.color.red_200))
                initiated = true
            }

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                if (!initiated)
                    init()

                if (parent.itemAnimator?.isRunning == true) {
                    var lastViewComingDown: View? = null
                    var firstViewComingUp: View? = null

                    var top = 0
                    var bottom = 0

                    val childCount = parent.layoutManager?.childCount ?: 0
                    for (i: Int in 0 until childCount) {
                        val child = parent.layoutManager!!.getChildAt(i)
                        if (child!!.translationY < 0)
                            lastViewComingDown = child
                        else if (child.translationY > 0 && firstViewComingUp == null)
                            firstViewComingUp = child
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    } else if (lastViewComingDown != null) {
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = lastViewComingDown.bottom
                    } else if (firstViewComingUp != null) {
                        top = firstViewComingUp.top
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    }

                    background.setBounds(0, top, parent.width, bottom)
                    background.draw(c)
                }

                super.onDraw(c, parent, state)
            }
        })
    }

    private fun onRemoveMovementInvoked(taggedSubscription: TaggedSubscription) {
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