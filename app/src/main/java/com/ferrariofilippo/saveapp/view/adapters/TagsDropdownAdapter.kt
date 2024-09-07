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
import com.ferrariofilippo.saveapp.model.entities.Tag

class TagsDropdownAdapter(context: Context, layoutId: Int, private val items: Array<Tag>) :
    ArrayAdapter<Tag>(context, layoutId, items), Filterable {

    data class TagViewHolder(val name: TextView, val pill: ImageView)

    private val _inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        var viewHolder: TagViewHolder? = null

        if (convertView == null) {
            view = _inflater.inflate(R.layout.tag_dropdown_item, null, true)

            if (view != null) {
                viewHolder = TagViewHolder(
                    view.findViewById(R.id.tag_name),
                    view.findViewById(R.id.tag_pill)
                )
                view.tag = viewHolder
            }
        } else {
            view = convertView
            viewHolder = view.tag as? TagViewHolder
        }

        if (viewHolder != null) {
            val tag = getItem(position)
            if (tag != null) {
                viewHolder.name.text = tag.fullName
                viewHolder.pill.setColorFilter(tag.color)
            }
        }

        return view!!
    }

    override fun getFilter(): Filter {
        val filter: Filter = object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Tag).fullName
            }

            // All tags should always be visible. Do not filter them
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
