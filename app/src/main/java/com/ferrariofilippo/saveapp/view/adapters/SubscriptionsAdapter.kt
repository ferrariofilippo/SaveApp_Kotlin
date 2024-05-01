// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import com.google.android.material.button.MaterialButton
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.snackbar.Snackbar

class SubscriptionsAdapter(
    private val ctx: Context,
    private val currency: Currencies,
    private val padding: Array<Int>
) : ListAdapter<TaggedSubscription, SubscriptionsAdapter.SubscriptionViewHolder>(
    SubscriptionComparator()
) {
    // Overrides
    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        return SubscriptionViewHolder.create(parent, ctx, currency, padding)
    }

    // Methods
    fun getItemAt(position: Int): TaggedSubscription {
        return getItem(position)
    }

    // Inner classes
    class SubscriptionViewHolder(
        itemView: View,
        private val ctx: Context,
        private val currency: Currencies,
        padding: Array<Int>
    ) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.subscriptionContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.subscriptionsDivider)
        private val amountItemView = itemView.findViewById<TextView>(R.id.subscriptionAmount)
        private val descriptionItemView =
            itemView.findViewById<TextView>(R.id.subscriptionDescription)
        private val nextRenewalItemView =
            itemView.findViewById<TextView>(R.id.subscriptionNextRenewal)
        private val renewalTypeItemView =
            itemView.findViewById<TextView>(R.id.subscriptionRenewalType)
        private val tagItemView = itemView.findViewById<MaterialButton>(R.id.subscriptionTagButton)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        fun bind(item: TaggedSubscription) {
            amountItemView.text = String.format("%s %.2f", currency.toSymbol(), item.amount)
            descriptionItemView.text = item.description
            nextRenewalItemView.text = item.nextRenewal.toString()
            renewalTypeItemView.text = getLocalizedRenewalType(item.renewalType)
            tagItemView.text = item.tagName
            tagItemView.setIconTintResource(item.tagColor)
            tagItemView.setStrokeColorResource(item.tagColor)
            tagItemView.setOnClickListener {
                Snackbar.make(
                    itemView.rootView.findViewById(R.id.containerView),
                    item.tagName,
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(itemView.rootView.findViewById(R.id.bottomAppBar)).show()
            }
        }

        private fun getLocalizedRenewalType(type: RenewalType): String {
            return when (type) {
                RenewalType.WEEKLY -> ctx.getString(R.string.WEEKLY)
                RenewalType.MONTHLY -> ctx.getString(R.string.MONTHLY)
                RenewalType.BIMONTHLY -> ctx.getString(R.string.BIMONTHLY)
                RenewalType.QUARTERLY -> ctx.getString(R.string.QUARTERLY)
                RenewalType.SEMIANNUALLY -> ctx.getString(R.string.SEMIANNUALLY)
                else -> ctx.getString(R.string.YEARLY)
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                ctx: Context,
                currency: Currencies,
                padding: Array<Int>
            ): SubscriptionViewHolder {
                val view: View = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.subscription_item, parent, false)

                return SubscriptionViewHolder(view, ctx, currency, padding)
            }
        }
    }

    class SubscriptionComparator : DiffUtil.ItemCallback<TaggedSubscription>() {
        override fun areContentsTheSame(
            oldItem: TaggedSubscription,
            newItem: TaggedSubscription
        ): Boolean {
            return oldItem.description == newItem.description &&
                    oldItem.amount == newItem.amount &&
                    oldItem.renewalType == newItem.renewalType
        }

        override fun areItemsTheSame(
            oldItem: TaggedSubscription,
            newItem: TaggedSubscription
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
