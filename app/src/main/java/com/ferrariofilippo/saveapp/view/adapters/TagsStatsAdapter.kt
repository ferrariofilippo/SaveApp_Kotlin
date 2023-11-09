package com.ferrariofilippo.saveapp.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.statsitems.TagMovementsSum

class TagsStatsAdapter(private val context: Context, private val currency: Currencies) :
    ListAdapter<TagMovementsSum, TagsStatsAdapter.TagsStatsViewHolder>(TagsStatsComparator()) {

    override fun onBindViewHolder(holder: TagsStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsStatsViewHolder {
        return TagsStatsViewHolder.create(parent, currency, context)
    }

    class TagsStatsViewHolder(
        itemView: View,
        private val currency: Currencies,
        private val ctx: Context
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val tagPillItemView = itemView.findViewById<ImageView>(R.id.byTagTagColorPill)
        private val tagNameItemView = itemView.findViewById<TextView>(R.id.byTagTagName)
        private val sumItemView = itemView.findViewById<TextView>(R.id.byTagSumTextView)

        @SuppressLint("SetTextI18n")
        fun bind(item: TagMovementsSum) {
            tagPillItemView.setColorFilter(ctx.getColor(item.color))
            tagNameItemView.text = item.name
            sumItemView.text =
                String.format("(%.1f %%) %s %.2f", item.percentage, currency.toSymbol(), item.sum)
        }

        companion object {
            fun create(parent: ViewGroup, currency: Currencies, ctx: Context): TagsStatsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tag_stats_item, parent, false)

                return TagsStatsViewHolder(view, currency, ctx)
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
