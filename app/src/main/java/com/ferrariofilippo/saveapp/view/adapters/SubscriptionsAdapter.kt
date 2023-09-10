package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import com.google.android.material.button.MaterialButton

class SubscriptionsAdapter(private val ctx: Context, private val currency: Currencies) :
    ListAdapter<TaggedSubscription, SubscriptionsAdapter.SubscriptionViewHolder>(
        SubscriptionComparator()
    ) {
    // Overrides
    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        return SubscriptionViewHolder.create(parent, ctx, currency)
    }

    // Methods
    fun getItemAt(position: Int): TaggedSubscription {
        return getItem(position)
    }


    // Inner classes
    class SubscriptionViewHolder(
        itemView: View,
        private val ctx: Context,
        private val currency: Currencies
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val amountItemView = itemView.findViewById<TextView>(R.id.subscriptionAmount)
        private val descriptionItemView =
            itemView.findViewById<TextView>(R.id.subscriptionDescription)

        private val nextRenewalItemView =
            itemView.findViewById<TextView>(R.id.subscriptionNextRenewal)

        private val renewalTypeItemView =
            itemView.findViewById<TextView>(R.id.subscriptionRenewalType)

        private val tagItemView = itemView.findViewById<MaterialButton>(R.id.subscriptionTagButton)

        fun bind(item: TaggedSubscription) {
            amountItemView.text = String.format("%s %.2f", currency.toSymbol(), item.amount)
            descriptionItemView.text = item.description
            nextRenewalItemView.text = item.nextRenewal.toString()
            renewalTypeItemView.text = getLocalizedRenewalType(item.renewalType)
            tagItemView.text = item.tagName
            tagItemView.setIconTintResource(item.tagColor)
            tagItemView.setStrokeColorResource(item.tagColor)
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
                currency: Currencies
            ): SubscriptionViewHolder {
                val view: View = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.subscription_item, parent, false)

                return SubscriptionViewHolder(view, ctx, currency)
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
