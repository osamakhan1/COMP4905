package com.comp4905.triviagameapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(private val listItem: List<ListItem>, private val listener : OnItemClickListener) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.category_list,
        parent, false)

        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = listItem[position]

        holder.categoryView.text = currentItem.text
    }

    override fun getItemCount() = listItem.size

    inner class CategoryViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
        val categoryView: TextView = itemView.findViewById(R.id.category_1)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position!= RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


}