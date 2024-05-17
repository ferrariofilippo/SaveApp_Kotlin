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
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.util.ColorUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar

class BudgetsAdapter(
    private val currency: Currencies,
    private val from: String,
    private val to: String,
    private val padding: Array<Int>
) : ListAdapter<TaggedBudget, BudgetsAdapter.BudgetViewHolder>(BudgetsComparator()) {

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return BudgetViewHolder.create(parent, currency, from, to, padding)
    }

    fun getItemAt(position: Int): TaggedBudget {
        return getItem(position)
    }

    class BudgetViewHolder(
        itemView: View,
        private val currency: Currencies,
        private val from: String,
        private val to: String,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.budgetContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.budgetsDivider)
        private val nameItemView = itemView.findViewById<TextView>(R.id.budgetName)
        private val dateFromItemView = itemView.findViewById<TextView>(R.id.budgetDateFrom)
        private val dateToItemView = itemView.findViewById<TextView>(R.id.budgetDateTo)
        private val tagItemView = itemView.findViewById<MaterialButton>(R.id.budgetTagButton)
        private val progressItemView =
            itemView.findViewById<LinearProgressIndicator>(R.id.budgetProgress)
        private val usedItemView = itemView.findViewById<TextView>(R.id.budgetUsed)
        private val maxItemView = itemView.findViewById<TextView>(R.id.budgetMax)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TaggedBudget) {
            val colorRes = ColorUtil.getColorIdFromHex(item.tagColor)
            nameItemView.text = item.name
            dateFromItemView.text = String.format(from, item.from.toString())
            dateToItemView.text = String.format(to, item.to.toString())
            tagItemView.text = item.tagName
            tagItemView.setIconTintResource(colorRes)
            tagItemView.setStrokeColorResource(colorRes)
            tagItemView.setOnClickListener {
                Snackbar.make(
                    itemView.rootView.findViewById(R.id.containerView),
                    item.tagName,
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(itemView.rootView.findViewById(R.id.bottomAppBar)).show()
            }
            progressItemView.progress = (item.used * 100.0 / item.max).toInt()
            usedItemView.text = "${currency.toSymbol()} ${String.format("%.2f", item.used)}"
            maxItemView.text = "${currency.toSymbol()} ${String.format("%.2f", item.max)}"
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
                from: String,
                to: String,
                padding: Array<Int>
            ): BudgetViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.budget_item, parent, false)

                return BudgetViewHolder(view, currency, from, to, padding)
            }
        }
    }

    class BudgetsComparator : DiffUtil.ItemCallback<TaggedBudget>() {
        override fun areContentsTheSame(oldItem: TaggedBudget, newItem: TaggedBudget): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.max == newItem.max &&
                    oldItem.used == newItem.used &&
                    oldItem.from == newItem.from &&
                    oldItem.to == newItem.to &&
                    oldItem.tagId == newItem.tagId
        }

        override fun areItemsTheSame(oldItem: TaggedBudget, newItem: TaggedBudget): Boolean {
            return oldItem.budgetId == newItem.budgetId
        }
    }
}
