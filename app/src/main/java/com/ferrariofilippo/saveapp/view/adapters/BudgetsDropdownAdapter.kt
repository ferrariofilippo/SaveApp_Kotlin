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
import android.widget.ImageView
import android.widget.TextView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import com.ferrariofilippo.saveapp.util.ColorUtil

class BudgetsDropdownAdapter(
    context: Context,
    layoutId: Int,
    private val items: Array<TaggedBudget>
) : ArrayAdapter<TaggedBudget>(context, layoutId, items),
    Filterable {

    data class BudgetViewHolder(val name: TextView, val endDate: TextView, val pill: ImageView)

    private val _inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        var viewHolder: BudgetViewHolder? = null

        if (convertView == null) {
            view = _inflater.inflate(R.layout.budget_dropdown_item, null, true)

            if (view != null) {
                viewHolder = BudgetViewHolder(
                    view.findViewById(R.id.budget_name),
                    view.findViewById(R.id.budget_end_date),
                    view.findViewById(R.id.budget_tag_pill)
                )
                view.tag = viewHolder
            }
        } else {
            view = convertView
            viewHolder = view.tag as? BudgetViewHolder
        }

        if (viewHolder != null) {
            val budget = getItem(position)
            if (budget != null) {
                viewHolder.name.text = budget.name
                viewHolder.endDate.text = budget.to.toString()
                viewHolder.pill.setColorFilter(
                    ColorUtil.getColorOrDefault(
                        context,
                        budget.tagColor
                    )
                )
            }
        }

        return view!!
    }

    override fun getFilter(): Filter {
        val filter: Filter = object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as TaggedBudget).name
            }

            // All budgets should always be visible. Do not filter them
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
}
