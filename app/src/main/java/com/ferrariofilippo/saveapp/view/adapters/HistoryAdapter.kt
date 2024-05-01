// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.snackbar.Snackbar

class HistoryAdapter(private val currency: Currencies, private val padding: Array<Int>) :
    ListAdapter<TaggedMovement, HistoryAdapter.MovementViewHolder>(MovementsComparator()) {
    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        return MovementViewHolder.create(parent, currency, padding)
    }

    fun getItemAt(position: Int): TaggedMovement {
        return getItem(position)
    }

    class MovementViewHolder(
        itemView: View,
        private val currency: Currencies,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.movementContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.historyDivider)
        private val amountItemView = itemView.findViewById<TextView>(R.id.movementAmount)
        private val descriptionItemView = itemView.findViewById<TextView>(R.id.movementDescription)
        private val dateItemView = itemView.findViewById<TextView>(R.id.movementDate)
        private val tagButtonItemView =
            itemView.findViewById<MaterialButton>(R.id.movementTagButton)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TaggedMovement) {
            amountItemView.text = "${currency.toSymbol()} ${String.format("%.2f", item.amount)}"
            descriptionItemView.text = item.description
            dateItemView.text = item.date.toString()
            tagButtonItemView.text = item.tagName
            tagButtonItemView.setIconTintResource(item.tagColor)
            tagButtonItemView.setStrokeColorResource(item.tagColor)
            tagButtonItemView.setOnClickListener {
                Snackbar.make(
                    itemView.rootView.findViewById(R.id.containerView),
                    item.tagName,
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(itemView.rootView.findViewById(R.id.bottomAppBar)).show()
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
                padding: Array<Int>
            ): MovementViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.movement_item, parent, false)

                return MovementViewHolder(view, currency, padding)
            }
        }
    }

    class MovementsComparator : DiffUtil.ItemCallback<TaggedMovement>() {
        override fun areContentsTheSame(oldItem: TaggedMovement, newItem: TaggedMovement): Boolean {
            return oldItem.description == newItem.description && oldItem.amount == newItem.amount
        }

        override fun areItemsTheSame(oldItem: TaggedMovement, newItem: TaggedMovement): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
