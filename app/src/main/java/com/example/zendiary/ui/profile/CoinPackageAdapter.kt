package com.example.zendiary.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

import com.example.zendiary.R


class CoinPackageAdapter(
    private val packages: List<CoinPackage>,
    private val onPackageClick: (CoinPackage) -> Unit
) : RecyclerView.Adapter<CoinPackageAdapter.CoinPackageViewHolder>() {

    inner class CoinPackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packageName: TextView = itemView.findViewById(R.id.package_name)
        val packageCoins: TextView = itemView.findViewById(R.id.package_coins)
        val packagePrice: TextView = itemView.findViewById(R.id.package_price)

        fun bind(coinPackage: CoinPackage) {
            packageName.text = coinPackage.name
            packageCoins.text = "${coinPackage.coins} Coins"
            packagePrice.text = "${coinPackage.price} VND"
            itemView.setOnClickListener { onPackageClick(coinPackage) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinPackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin_package, parent, false)
        return CoinPackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinPackageViewHolder, position: Int) {
        holder.bind(packages[position])
    }

    override fun getItemCount(): Int = packages.size
}
