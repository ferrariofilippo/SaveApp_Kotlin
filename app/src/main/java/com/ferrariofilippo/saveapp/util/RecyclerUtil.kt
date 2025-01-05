// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import kotlin.math.absoluteValue

class CustomRecyclerDecorator(private val ctx: Context) : RecyclerView.ItemDecoration() {
    private lateinit var background: Drawable
    private var initiated: Boolean = false

    private fun init() {
        background =
            ColorDrawable(ContextCompat.getColor(ctx, R.color.red_200))
        initiated = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (!initiated) {
            init()
        }

        if (parent.itemAnimator?.isRunning == true) {
            var lastViewComingDown: View? = null
            var firstViewComingUp: View? = null

            var top = 0
            var bottom = 0

            val childCount = parent.layoutManager?.childCount ?: 0
            for (i: Int in 0 until childCount) {
                val child = parent.layoutManager!!.getChildAt(i)
                if (child!!.translationY < 0) {
                    lastViewComingDown = child
                } else if (child.translationY > 0 && firstViewComingUp == null) {
                    firstViewComingUp = child
                }
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
}

abstract class RecyclerEditAndDeleteGestures(private val ctx: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private lateinit var deleteBackground: Drawable
    private lateinit var deleteIcon: Drawable
    private lateinit var editBackground: Drawable
    private lateinit var editIcon: Drawable
    private var iconMargin: Int = (16.0f * ctx.resources.displayMetrics.density + 0.5f).toInt()
    private var initiated: Boolean = false

    var editOnlyRow = -1

    private fun init() {
        val deleteColor = TypedValue()
        val editColor = TypedValue()
        ctx.theme.resolveAttribute(com.google.android.material.R.attr.colorError, deleteColor, true)
        ctx.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimaryContainer,
            editColor,
            true
        )
        deleteBackground = ColorDrawable(deleteColor.data)
        editBackground = ColorDrawable(editColor.data)

        deleteIcon = ContextCompat.getDrawable(ctx, R.drawable.baseline_delete_24)!!
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

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder.adapterPosition == -1 || (dX < 0 && viewHolder.adapterPosition == editOnlyRow)) {
            return
        }

        if (!initiated) {
            init()
        }

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

            if (dX.absoluteValue > iconMargin + icon.intrinsicWidth) {
                val height = viewHolder.itemView.bottom - viewHolder.itemView.top
                val iconTop = viewHolder.itemView.top + (height - icon.intrinsicHeight) / 2
                icon.setBounds(iconLeft, iconTop, iconRight, iconTop + icon.intrinsicHeight)
                icon.draw(c)
            }
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
