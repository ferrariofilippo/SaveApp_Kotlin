package com.ferrariofilippo.saveapp.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.entities.Tag

class TagsAdapter(private val ctx: Context) :
    ListAdapter<Tag, TagsAdapter.TagViewHolder>(TagsComparator()) {
    // Overrides
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder.create(parent, ctx)
    }

    // Methods
    fun getItemAt(position: Int): Tag {
        return getItem(position)
    }

    // Inner classes
    class TagViewHolder(itemView: View, private val ctx: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val tagNameItemView = itemView.findViewById<TextView>(R.id.list_tag_name)
        private val tagPillItemView = itemView.findViewById<ImageView>(R.id.list_tag_pill)

        fun bind(item: Tag) {
            tagNameItemView.text = item.name
            tagPillItemView.setColorFilter(ContextCompat.getColor(ctx, item.color))
        }

        companion object {
            fun create(parent: ViewGroup, ctx: Context): TagViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.tag_item, parent, false)

                return TagViewHolder(view, ctx)
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
