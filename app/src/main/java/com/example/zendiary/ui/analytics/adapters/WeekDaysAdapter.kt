package com.example.zendiary.ui.analytics.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R

class WeekDaysAdapter(
    private val onDayClick: (String) -> Unit // Callback for when a day is clicked
) : ListAdapter<String, WeekDaysAdapter.WeekDayViewHolder>(DIFF_CALLBACK) {

    private var selectedDay: Int = -1 // Tracks the selected day index

    inner class WeekDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.tvDayName)
        val dateText: TextView = itemView.findViewById(R.id.tvDayDate)

        fun bind(day: String, position: Int) {
            // Split the day string into day name and date (e.g., "M 29")
            val parts = day.split(" ")
            val dayName = parts[0]  // "M"
            val dayDate = parts[1]  // "29"

            // Set the text for day name and date
            dayText.text = dayName
            dateText.text = dayDate

            // Update the selected state
            itemView.isSelected = position == selectedDay
            itemView.setBackgroundResource(
                if (itemView.isSelected) R.drawable.selected_day_background else android.R.color.transparent
            )

            // Handle item click
            itemView.setOnClickListener {
                val previousSelectedDay = selectedDay
                selectedDay = position

                // Notify RecyclerView to refresh only the affected items
                notifyItemChanged(previousSelectedDay)
                notifyItemChanged(selectedDay)

                // Invoke the click callback
                onDayClick(day)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return WeekDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekDayViewHolder, position: Int) {
        val day = getItem(position) // Get day from the current list
        holder.bind(day, position)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}
