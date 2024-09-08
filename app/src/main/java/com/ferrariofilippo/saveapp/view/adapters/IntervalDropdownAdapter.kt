// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.enums.PeriodicInterval

class IntervalDropdownAdapter(
    context: Context,
    layoutId: Int,
    private val items: Array<PeriodicInterval>
) :
    ArrayAdapter<PeriodicInterval>(context, layoutId, items), Filterable {

    data class IntervalViewHolder(val name: TextView)

    private val _inflater = LayoutInflater.from(context)

    // Overrides
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        var viewHolder: IntervalViewHolder? = null

        if (convertView == null) {
            view = _inflater.inflate(R.layout.renewal_dropdown_item, null, true)

            if (view != null) {
                viewHolder = IntervalViewHolder(view.findViewById(R.id.renewal_name))
                view.tag = viewHolder
            }
        } else {
            view = convertView
            viewHolder = view.tag as? IntervalViewHolder
        }

        if (viewHolder != null) {
            val interval = getItem(position)
            if (interval != null) {
                viewHolder.name.text = getLocalizedName(interval)
            }
        }

        return view!!
    }

    override fun getFilter(): Filter {
        val filter: Filter = object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return getLocalizedName(resultValue as PeriodicInterval)
            }

            // All intervals should always be visible. Do not filter them
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val results = FilterResults()

                results.values = items
                results.count = items.size

                return results
            }

            override fun publishResults(costraint: CharSequence?, results: FilterResults?) {
            }
        }

        return filter
    }

    // Methods
    fun getLocalizedName(type: PeriodicInterval): String {
        return when (type) {
            PeriodicInterval.DAILY -> context.getString(R.string.DAILY)
            PeriodicInterval.TWO_DAYS -> context.getString(R.string.TWO_DAYS)
            PeriodicInterval.WEEKLY -> context.getString(R.string.WEEKLY)
            PeriodicInterval.TWO_WEEKS -> context.getString(R.string.TWO_WEEKS)
            else -> context.getString(R.string.MONTHLY)
        }
    }
}
