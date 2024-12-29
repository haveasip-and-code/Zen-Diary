package com.example.zendiary.ui.analytics.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.ui.analytics.models.DayPreview

class DayPreviewAdapter(
    private val dayPreviews: MutableList<DayPreview> = mutableListOf()
) : RecyclerView.Adapter<DayPreviewAdapter.DayPreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayPreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_preview, parent, false)
        return DayPreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayPreviewViewHolder, position: Int) {
        val dayPreview = dayPreviews[position]
        holder.bind(dayPreview)
    }

    override fun getItemCount(): Int = dayPreviews.size

    fun submitList(newDayPreviews: List<DayPreview>) {
        dayPreviews.clear()
        dayPreviews.addAll(newDayPreviews)
        notifyDataSetChanged()
    }

    class DayPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tv_header) // Journal Title
        private val tvPreview: TextView = itemView.findViewById(R.id.tv_preview) // Journal Content Snippet
        private val ibImagePreview: ImageView = itemView.findViewById(R.id.ib_image_preview) // Mood Icon
        private val tvAnalyticsResult: TextView = itemView.findViewById(R.id.tv_analytics_result) // Journal Date

        fun bind(dayPreview: DayPreview) {
            tvHeader.text = dayPreview.title
            tvPreview.text = dayPreview.snippet
            tvAnalyticsResult.text = dayPreview.result

            // Load the mood icon dynamically
            ibImagePreview.setImageResource(dayPreview.moodIconResId)
        }
    }
}
