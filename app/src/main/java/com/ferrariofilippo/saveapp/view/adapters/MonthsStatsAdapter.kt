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
import com.ferrariofilippo.saveapp.model.statsitems.MonthTransactionsSum
import com.google.android.material.divider.MaterialDivider

class MonthsStatsAdapter(private val currency: Currencies, private val padding: Array<Int>) :
    ListAdapter<MonthTransactionsSum, MonthsStatsAdapter.MonthsStatsViewHolder>(MonthsStatsComparator()) {
    override fun onBindViewHolder(holder: MonthsStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsStatsViewHolder {
        return MonthsStatsViewHolder.create(parent, currency, padding)
    }

    class MonthsStatsViewHolder(
        itemView: View,
        private val currency: Currencies,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.monthStatsContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.monthStatsDivider)
        private val monthNameItemView = itemView.findViewById<TextView>(R.id.byMonthMonthName)
        private val sumItemView = itemView.findViewById<TextView>(R.id.byMonthSumTextView)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: MonthTransactionsSum) {
            monthNameItemView.text = item.name
            sumItemView.text =
                String.format("(%.1f %%) %s %.2f", item.percentage, currency.toSymbol(), item.sum)
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
                padding: Array<Int>
            ): MonthsStatsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.month_stats_item, parent, false)

                return MonthsStatsViewHolder(view, currency, padding)
            }
        }
    }

    class MonthsStatsComparator : DiffUtil.ItemCallback<MonthTransactionsSum>() {
        override fun areContentsTheSame(
            oldItem: MonthTransactionsSum,
            newItem: MonthTransactionsSum
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.sum == newItem.sum
        }

        override fun areItemsTheSame(
            oldItem: MonthTransactionsSum,
            newItem: MonthTransactionsSum
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
