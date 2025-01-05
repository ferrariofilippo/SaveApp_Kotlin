// Copyright (c) 2025 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import com.ferrariofilippo.saveapp.util.ColorUtil
import com.ferrariofilippo.saveapp.util.DateUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.snackbar.Snackbar

class HistoryAdapter(private val currency: Currencies, private val padding: Array<Int>) :
    ListAdapter<TaggedTransaction, HistoryAdapter.TransactionViewHolder>(TransactionsComparator()) {
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder.create(parent, currency, padding)
    }

    fun getItemAt(position: Int): TaggedTransaction {
        return getItem(position)
    }

    class TransactionViewHolder(
        itemView: View,
        private val currency: Currencies,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.transactionContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.historyDivider)
        private val amountItemView = itemView.findViewById<TextView>(R.id.transactionAmount)
        private val descriptionItemView =
            itemView.findViewById<TextView>(R.id.transactionDescription)
        private val dateItemView = itemView.findViewById<TextView>(R.id.transactionDate)
        private val tagButtonItemView =
            itemView.findViewById<MaterialButton>(R.id.transactionTagButton)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TaggedTransaction) {
            val locale = java.util.Locale.getDefault()
            val colorRes = ColorUtil.getColorIdFromHex(item.tagColor)
            amountItemView.text =
                "${currency.toSymbol()} ${String.format(locale, "%.2f", item.amount)}"
            descriptionItemView.text = item.description
            dateItemView.text = item.date.format(DateUtil.getFormatter(locale))
            tagButtonItemView.text = item.tagName
            tagButtonItemView.setIconTintResource(colorRes)
            tagButtonItemView.setStrokeColorResource(colorRes)
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
            ): TransactionViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.transaction_item, parent, false)

                return TransactionViewHolder(view, currency, padding)
            }
        }
    }

    class TransactionsComparator : DiffUtil.ItemCallback<TaggedTransaction>() {
        override fun areContentsTheSame(
            oldItem: TaggedTransaction,
            newItem: TaggedTransaction
        ): Boolean {
            return oldItem.description == newItem.description &&
                    oldItem.amount == newItem.amount &&
                    oldItem.date == newItem.date &&
                    oldItem.tagId == newItem.tagId
        }

        override fun areItemsTheSame(
            oldItem: TaggedTransaction,
            newItem: TaggedTransaction
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
