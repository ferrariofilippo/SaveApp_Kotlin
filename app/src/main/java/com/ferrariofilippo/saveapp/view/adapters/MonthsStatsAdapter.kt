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
import com.ferrariofilippo.saveapp.model.statsitems.MonthMovementsSum

class MonthsStatsAdapter(private val currency: Currencies) :
    ListAdapter<MonthMovementsSum, MonthsStatsAdapter.MonthsStatsViewHolder>(MonthsStatsComparator()) {
    override fun onBindViewHolder(holder: MonthsStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsStatsViewHolder {
        return MonthsStatsViewHolder.create(parent, currency)
    }

    class MonthsStatsViewHolder(
        itemView: View,
        private val currency: Currencies,
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val monthNameItemView = itemView.findViewById<TextView>(R.id.byMonthMonthName)
        private val sumItemView = itemView.findViewById<TextView>(R.id.byMonthSumTextView)

        @SuppressLint("SetTextI18n")
        fun bind(item: MonthMovementsSum) {
            monthNameItemView.text = item.name
            sumItemView.text = String.format("%s %.2f", currency.toSymbol(), item.sum)
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
            ): MonthsStatsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.month_stats_item, parent, false)

                return MonthsStatsViewHolder(view, currency)
            }
        }
    }

    class MonthsStatsComparator : DiffUtil.ItemCallback<MonthMovementsSum>() {
        override fun areContentsTheSame(
            oldItem: MonthMovementsSum,
            newItem: MonthMovementsSum
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.sum == newItem.sum
        }

        override fun areItemsTheSame(
            oldItem: MonthMovementsSum,
            newItem: MonthMovementsSum
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
