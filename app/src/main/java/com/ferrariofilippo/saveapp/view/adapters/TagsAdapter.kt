// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.google.android.material.divider.MaterialDivider

class TagsAdapter(private val ctx: Context, private val padding: Array<Int>) :
    ListAdapter<Tag, TagsAdapter.TagViewHolder>(TagsComparator()) {
    // Overrides
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder.create(parent, ctx, padding)
    }

    // Methods
    fun getItemAt(position: Int): Tag {
        return getItem(position)
    }

    // Inner classes
    class TagViewHolder(itemView: View, private val ctx: Context, padding: Array<Int>) :
        RecyclerView.ViewHolder(itemView) {
        private val container = itemView.findViewById<RelativeLayout>(R.id.tagContainer)
        private val divider = itemView.findViewById<MaterialDivider>(R.id.tagsDivider)
        private val tagNameItemView = itemView.findViewById<TextView>(R.id.list_tag_name)
        private val tagPillItemView = itemView.findViewById<ImageView>(R.id.list_tag_pill)

        init {
            val dividerParams = divider.layoutParams as ViewGroup.MarginLayoutParams
            dividerParams.topMargin = padding[1]
            divider.layoutParams = dividerParams
            container.setPadding(padding[0], padding[1], padding[0], 0)
        }

        fun bind(item: Tag) {
            tagNameItemView.text = item.name
            tagPillItemView.setColorFilter(ContextCompat.getColor(ctx, item.color))
        }

        companion object {
            fun create(parent: ViewGroup, ctx: Context, padding: Array<Int>): TagViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.tag_item, parent, false)

                return TagViewHolder(view, ctx, padding)
            }
        }
    }

    class TagsComparator : DiffUtil.ItemCallback<Tag>() {
        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.name == newItem.name && oldItem.color == newItem.color
        }

        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
