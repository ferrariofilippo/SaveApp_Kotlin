package com.ferrariofilippo.saveapp.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class BudgetsAdapter(
    private val currency: Currencies,
    private val from: String,
    private val to: String
) :
    ListAdapter<TaggedBudget, BudgetsAdapter.BudgetViewHolder>(BudgetsComparator()) {

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return BudgetViewHolder.create(parent, currency, from, to)
    }

    class BudgetViewHolder(
        itemView: View,
        private val currency: Currencies,
        private val from: String,
        private val to: String
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val nameItemView = itemView.findViewById<TextView>(R.id.budgetName)
        private val dateFromItemView = itemView.findViewById<TextView>(R.id.budgetDateFrom)
        private val dateToItemView = itemView.findViewById<TextView>(R.id.budgetDateTo)
        private val tagItemView = itemView.findViewById<MaterialButton>(R.id.budgetTagButton)
        private val progressItemView =
            itemView.findViewById<LinearProgressIndicator>(R.id.budgetProgress)
        private val maxItemView = itemView.findViewById<TextView>(R.id.budgetMax)

        @SuppressLint("SetTextI18n")
        fun bind(item: TaggedBudget) {
            nameItemView.text = item.name
            dateFromItemView.text = String.format(from, item.from.toString())
            dateToItemView.text = String.format(to, item.to.toString())
            tagItemView.text = item.tagName
            tagItemView.setIconTintResource(item.tagColor)
            tagItemView.setStrokeColorResource(item.tagColor)
            progressItemView.progress = (item.used * 100.0 / item.max).toInt()
            maxItemView.text = "${currency.toSymbol()} ${String.format("%.2f", item.max)}"
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
                from: String,
                to: String
            ): BudgetViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.budget_item, parent, false)

                return BudgetViewHolder(view, currency, from, to)
            }
        }
    }

    class BudgetsComparator : DiffUtil.ItemCallback<TaggedBudget>() {
        override fun areContentsTheSame(oldItem: TaggedBudget, newItem: TaggedBudget): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.max == oldItem.max &&
                    oldItem.used == newItem.used &&
                    oldItem.from == newItem.from &&
                    oldItem.to == newItem.to
        }

        override fun areItemsTheSame(oldItem: TaggedBudget, newItem: TaggedBudget): Boolean {
            return oldItem.budgetId == newItem.budgetId
        }
    }
}
