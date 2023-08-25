package com.ferrariofilippo.saveapp.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement

class HistoryAdapter :
    ListAdapter<TaggedMovement, HistoryAdapter.MovementViewHolder>(MovementsComparator()) {

    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        holder.bind(getItem(position));
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        return MovementViewHolder(parent);
    }

    class MovementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountItemView = itemView.findViewById<TextView>(R.id.movementAmount);
        private val descriptionItemView = itemView.findViewById<TextView>(R.id.movementDescription);
        private val dateItemView = itemView.findViewById<TextView>(R.id.movementDate);
        private val tagButtonItemView = itemView.findViewById<Button>(R.id.movementTagButton);

        @SuppressLint("SetTextI18n")
        fun bind(item: TaggedMovement) {
            amountItemView.text = "${"Currency"} ${item.amount}";
            descriptionItemView.text = item.description;
            dateItemView.text = item.date.toString();
            tagButtonItemView.text = item.tagName;
            tagButtonItemView.setTextColor(item.tagColor);
        }

        companion object {
            fun create(parent: ViewGroup): MovementViewHolder {
                val view: View =
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.movement_item, parent, false);

                return MovementViewHolder(view);
            }
        }
    }

    class MovementsComparator : DiffUtil.ItemCallback<TaggedMovement>() {
        override fun areContentsTheSame(oldItem: TaggedMovement, newItem: TaggedMovement): Boolean {
            return oldItem.description == newItem.description && oldItem.amount == newItem.amount;
        }

        override fun areItemsTheSame(oldItem: TaggedMovement, newItem: TaggedMovement): Boolean {
            return oldItem.id == newItem.id;
        }
    }
}