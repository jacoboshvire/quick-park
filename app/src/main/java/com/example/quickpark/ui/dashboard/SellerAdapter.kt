package com.example.quickpark.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.SellerItem

class SellerAdapter(
    private val onItemClick: (SellerItem) -> Unit
) : RecyclerView.Adapter<SellerAdapter.ViewHolder>() {

    private val items = mutableListOf<SellerItem>()

    fun submitList(data: List<SellerItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parking, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: SellerItem) {
            itemView.findViewById<TextView>(R.id.locationText).text = item.locations
            itemView.findViewById<TextView>(R.id.priceText).text = "£${item.price}"

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

