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
import com.ferrariofilippo.saveapp.model.statsitems.YearStats

class YearStatsAdapter(val currency: Currencies) :
    ListAdapter<YearStats, YearStatsAdapter.YearStatsViewHolder>(YearStatsComparator()) {
    override fun onBindViewHolder(holder: YearStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearStatsViewHolder {
        return YearStatsViewHolder.create(parent, currency)
    }

    class YearStatsViewHolder(itemView: View, private val currency: Currencies) :
        RecyclerView.ViewHolder(itemView) {
        private val yearItemView = itemView.findViewById<TextView>(R.id.byYearYear)
        private val expensesItemView = itemView.findViewById<TextView>(R.id.byYearExpensesValue)
        private val incomesItemView = itemView.findViewById<TextView>(R.id.byYearIncomesValue)

        @SuppressLint("SetTextI18n")
        fun bind(item: YearStats) {
            yearItemView.text = item.year.toString()
            expensesItemView.text = String.format("%s %.2f", currency.toSymbol(), item.expenses)
            incomesItemView.text = String.format("%s %.2f", currency.toSymbol(), item.incomes)
        }

        companion object {
            fun create(parent: ViewGroup, currency: Currencies): YearStatsViewHolder {
                val view: View = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.year_stats_item, parent, false)

                return YearStatsViewHolder(view, currency)
            }
        }
    }

    class YearStatsComparator : DiffUtil.ItemCallback<YearStats>() {
        override fun areContentsTheSame(oldItem: YearStats, newItem: YearStats): Boolean {
            return oldItem.year == newItem.year &&
                    oldItem.expenses == newItem.expenses &&
                    oldItem.incomes == newItem.incomes
        }

        override fun areItemsTheSame(oldItem: YearStats, newItem: YearStats): Boolean {
            return oldItem.year == newItem.year
        }
    }
}
