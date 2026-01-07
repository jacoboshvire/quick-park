package com.example.quickpark.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quickpark.R
import com.example.quickpark.data.network.SellerItem

class SellerAdapter : RecyclerView.Adapter<SellerAdapter.ViewHolder>() {

    private val items = mutableListOf<SellerItem>()

    fun submitList(data: List<SellerItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parking, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val locationText: TextView =
            itemView.findViewById(R.id.locationText)

        private val priceText: TextView =
            itemView.findViewById(R.id.priceText)

        fun bind(item: SellerItem) {
            locationText.text = item.locations
            priceText.text = "£${item.price}"
        }
    }
}
