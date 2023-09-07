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
import com.ferrariofilippo.saveapp.model.enums.RenewalType

class RenewalDropdownAdapter(
    context: Context,
    layoutId: Int,
    private val items: Array<RenewalType>
) :
    ArrayAdapter<RenewalType>(context, layoutId, items), Filterable {

    data class RenewalViewHolder(val name: TextView)

    private val _inflater = LayoutInflater.from(context)

    // Overrides
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        var viewHolder: RenewalViewHolder? = null

        if (convertView == null) {
            view = _inflater.inflate(R.layout.renewal_dropdown_item, null, true)

            if (view != null) {
                viewHolder = RenewalViewHolder(view.findViewById(R.id.renewal_name))
                view.tag = viewHolder
            }
        } else {
            view = convertView
            viewHolder = view.tag as? RenewalViewHolder
        }

        if (viewHolder != null) {
            val renewal = getItem(position)
            if (renewal != null)
                viewHolder.name.text = getLocalizedName(renewal)
        }

        return view!!
    }

    override fun getFilter(): Filter {
        val filter: Filter = object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return getLocalizedName(resultValue as RenewalType)
            }

            // All renewal types should always be visible. Do not filter them
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
    fun getLocalizedName(type: RenewalType): String {
        return when(type) {
            RenewalType.WEEKLY -> context.getString(R.string.WEEKLY)
            RenewalType.MONTHLY -> context.getString(R.string.MONTHLY)
            RenewalType.BIMONTHLY -> context.getString(R.string.BIMONTHLY)
            RenewalType.QUARTERLY -> context.getString(R.string.QUARTERLY)
            RenewalType.SEMIANNUALLY -> context.getString(R.string.SEMIANNUALLY)
            else -> context.getString(R.string.YEARLY)
        }
    }
}
