package com.example.zendiary.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zendiary.R
import com.example.zendiary.ui.analytics.models.Recommendation

class AnalyticsViewModel : ViewModel() {

    // LiveData for the list of recommendations
    private val _recommendations = MutableLiveData<List<Recommendation>>()
    val recommendations: LiveData<List<Recommendation>> get() = _recommendations

    // LiveData for selected day/week toggle state
    private val _isDayViewSelected = MutableLiveData<Boolean>(true) // Default to "Day"
    val isDayViewSelected: LiveData<Boolean> get() = _isDayViewSelected

    // LiveData for selected day (for day/week toggle logic)
    private val _selectedDay = MutableLiveData<String>()
    val selectedDay: LiveData<String> get() = _selectedDay

    // LiveData for the current mood flow and mood bar data
    private val _moodFlowData = MutableLiveData<List<Float>>() // Example mood flow data
    val moodFlowData: LiveData<List<Float>> get() = _moodFlowData

    private val _weekDays = MutableLiveData<List<String>>()
    val weekDays: LiveData<List<String>> get() = _weekDays

    private val _dateRange = MutableLiveData<Pair<Long, Long>>()
    val dateRange: LiveData<Pair<Long, Long>> get() = _dateRange

    init {
        // Load initial data
        loadRecommendations()
        loadMoodFlowData()
        // Initialize with example data
        _weekDays.value = listOf("M 29", "T 30", "W 31", "T 1", "F 2", "S 3", "S 4")
    }

    // Function to load recommendations
    private fun loadRecommendations() {
        _recommendations.value = listOf(
            Recommendation(
                result = "Positive",
                dishName = "Pumpkin Chicken Curry",
                dishNote = "Recommended for lunch time",
                imageResId = R.drawable.avatar // Replace with actual drawable resource
            ),
            Recommendation(
                result = "Neutral",
                dishName = "Spaghetti Bolognese",
                dishNote = "Good for evening meals",
                imageResId = R.drawable.avatar // Replace with actual drawable resource
            ),
            Recommendation(
                result = "Negative",
                dishName = "Mushroom Risotto",
                dishNote = "Avoid heavy meals during this time",
                imageResId = R.drawable.avatar // Replace with actual drawable resource
            )
        )
    }

    // Function to load mood flow data (placeholder example)
    private fun loadMoodFlowData() {
        _moodFlowData.value = listOf(4.5f, 3.0f, 4.0f, 5.0f, 2.5f, 4.0f, 3.5f) // Example scores
    }

    // Function to toggle between Day and Week views
    fun toggleDayWeekView(isDay: Boolean) {
        _isDayViewSelected.value = isDay
    }

    // Function to update the selected day
    fun updateSelectedDay(day: String) {
        _selectedDay.value = day
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }
}
