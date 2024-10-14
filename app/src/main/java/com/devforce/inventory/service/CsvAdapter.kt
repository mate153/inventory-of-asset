package com.devforce.inventory.service

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devforce.inventory.R

class CsvAdapter(private val data: List<List<String>>) :
    RecyclerView.Adapter<CsvAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    class ViewHolder(view: View, private val onItemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textView)

        fun bind(item: String, position: Int, selectedItemPosition: Int) {
            textView.text = item
            itemView.isActivated = position == selectedItemPosition

            if (position == selectedItemPosition) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                    R.color.selectedColor
                ))
            } else {
                if (position % 2 == 0) {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.light_grey
                    ))
                } else {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.darker_grey
                    ))
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(if (item.size >= 3) item[3] else item.firstOrNull().toString(), position, selectedItemPosition)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleSelection(position: Int) {
        selectedItemPosition = if (selectedItemPosition == position) {
            RecyclerView.NO_POSITION
        } else {
            position
        }
        notifyDataSetChanged()
    }
}