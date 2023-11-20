// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.ferrariofilippo.saveapp.R

class ColorDropdownAdapter(private val context: Context, layoutId: Int, items: Array<Int>) :
    ArrayAdapter<Int>(context, layoutId, items) {

    private val _inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        var viewHolder: ImageView? = null

        if (convertView == null) {
            view = _inflater.inflate(R.layout.color_dropdown_item, null, true)

            if (view != null) {
                viewHolder = view.findViewById(R.id.color_pill)
                view.tag = viewHolder
            }
        } else {
            view = convertView
            viewHolder = view.tag as? ImageView
        }

        if (viewHolder != null) {
            val color = getItem(position)
            if (color != null) {
                viewHolder.setColorFilter(ContextCompat.getColor(context, color))
            }
        }

        return view!!
    }
}
