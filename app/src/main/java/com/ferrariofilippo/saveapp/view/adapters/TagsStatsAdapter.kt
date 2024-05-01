// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.statsitems.TagMovementsSum
import com.google.android.material.divider.MaterialDivider

class TagsStatsAdapter(
    private val context: Context,
    private val currency: Currencies,
    private val padding: Array<Int>
) : ListAdapter<TagMovementsSum, TagsStatsAdapter.TagsStatsViewHolder>(TagsStatsComparator()) {

    override fun onBindViewHolder(holder: TagsStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsStatsViewHolder {
        return TagsStatsViewHolder.create(parent, currency, context, padding)
    }

    class TagsStatsViewHolder(
        itemView: View,
        private val currency: Currencies,
        private val ctx: Context,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<LinearLayoutCompat>(R.id.tagStatsContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.tagStatsDivider)
        private val tagPillItemView = itemView.findViewById<ImageView>(R.id.byTagTagColorPill)
        private val tagNameItemView = itemView.findViewById<TextView>(R.id.byTagTagName)
        private val sumItemView = itemView.findViewById<TextView>(R.id.byTagSumTextView)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TagMovementsSum) {
            tagPillItemView.setColorFilter(ctx.getColor(item.color))
            tagNameItemView.text = item.name
            sumItemView.text =
                String.format("(%.1f %%) %s %.2f", item.percentage, currency.toSymbol(), item.sum)
        }

        companion object {
            fun create(
                parent: ViewGroup,
                currency: Currencies,
                ctx: Context,
                padding: Array<Int>
            ): TagsStatsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tag_stats_item, parent, false)

                return TagsStatsViewHolder(view, currency, ctx, padding)
            }
        }
    }

    class TagsStatsComparator : DiffUtil.ItemCallback<TagMovementsSum>() {
        override fun areContentsTheSame(
            oldItem: TagMovementsSum,
            newItem: TagMovementsSum
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.sum == newItem.sum
        }

        override fun areItemsTheSame(oldItem: TagMovementsSum, newItem: TagMovementsSum): Boolean {
            return oldItem.tagId == newItem.tagId
        }
    }
}
