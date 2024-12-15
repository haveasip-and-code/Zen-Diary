package com.example.zendiary.ui.profile

import com.example.zendiary.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.ui.profile.StoreItem
import com.squareup.picasso.Picasso
import java.lang.String


class StoreAdapter(storeItems: List<StoreItem>,
                   private val onClick: (StoreItem) -> Unit // Add this
) :
    RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {
    private val storeItems: List<StoreItem> = storeItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val item: StoreItem = storeItems[position]

        holder.itemView.setOnClickListener { onClick(item) } // Call click listener

        holder.nameTextView.setText(item.getName())
        holder.priceTextView.text = String.format("%.2f", item.getPrice())

        // Load image using Picasso or Glide
        Picasso.get().load(item.getImageUrl()).into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return storeItems.size
    }

    class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView = itemView.findViewById<TextView>(R.id.item_name)
        var priceTextView: TextView = itemView.findViewById<TextView>(R.id.item_price)
        var imageView: ImageView = itemView.findViewById<ImageView>(R.id.item_image)
    }
}