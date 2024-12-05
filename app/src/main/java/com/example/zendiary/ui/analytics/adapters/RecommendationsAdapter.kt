package com.example.zendiary.ui.analytics.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.ui.analytics.models.Recommendation

class RecommendationsAdapter(
    private val recommendations: MutableList<Recommendation> = mutableListOf()
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation_menu, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]
        holder.bind(recommendation)
    }

    override fun getItemCount(): Int = recommendations.size

    fun submitList(newRecommendations: List<Recommendation>) {
        recommendations.clear()
        recommendations.addAll(newRecommendations)
        notifyDataSetChanged()
    }

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAnalyticsResult: TextView = itemView.findViewById(R.id.tv_analytics_result)
        private val tvDishName: TextView = itemView.findViewById(R.id.tv_dish_name)
        private val tvDishNote: TextView = itemView.findViewById(R.id.tv_dish_note)
        private val ibDishImage: ImageView = itemView.findViewById(R.id.ib_dish_image)

        fun bind(recommendation: Recommendation) {
            tvAnalyticsResult.text = recommendation.result
            tvDishName.text = recommendation.dishName
            tvDishNote.text = recommendation.dishNote

            // Use a placeholder image or load dynamically with an image loader like Glide or Picasso
            ibDishImage.setImageResource(recommendation.imageResId)
        }
    }
}
