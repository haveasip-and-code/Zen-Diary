package com.example.zendiary.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zendiary.R
import com.example.zendiary.data.FirebaseRepository
import com.example.zendiary.data.FirebaseRepository.getEntriesInDateRange
import com.example.zendiary.ui.analytics.models.Recommendation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
//        loadRecommendationsForDay("M")
        loadMoodFlowData()
        initializeWeekDays()
    }

    private fun initializeWeekDays() {
        val calendar = Calendar.getInstance()

        // Set the first day of the week to Monday (if not already set)
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Get the current day of the week
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ..., Saturday = 7
        when (currentDayOfWeek) {
            1 -> updateSelectedDay("Su")
            2 -> updateSelectedDay("M")
            3 -> updateSelectedDay("T")
            4 -> updateSelectedDay("W")
            5 -> updateSelectedDay("T")
            6 -> updateSelectedDay("F")
            7 -> updateSelectedDay("Sa")
        }

        // Find the offset to adjust the calendar to the start of the week (Monday)
        val daysToMonday = if (currentDayOfWeek == Calendar.SUNDAY) {
            -6 // If today is Sunday, move 6 days back to get to Monday
        } else {
            Calendar.MONDAY - currentDayOfWeek // Adjust for the rest of the days
        }

        // Set the calendar to the start of the week (Monday)
        calendar.add(Calendar.DAY_OF_YEAR, daysToMonday)

        // List to hold the week days
        val weekDays = mutableListOf<String>()

        // Format for displaying day of the month
        val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())

        // Add the days of the week starting from Monday
        for (i in 0 until 7) {
            val dayOfMonth = dayOfMonthFormat.format(calendar.time)

            // Get the weekday abbreviation
            val weekday = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "Sa"
                Calendar.SUNDAY -> "Su"
                else -> ""
            }

            // Add to the week days list
            weekDays.add("$weekday $dayOfMonth")

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the value in the LiveData or equivalent variable
        _weekDays.value = weekDays
    }

    // Data structure to hold dishes for each day
    private val weeklyDishes = mapOf(
        "M" to listOf(
            Recommendation("Positive", "Pumpkin Chicken Curry", "Recommended for lunch time", R.drawable.avatar),
            Recommendation("Neutral", "Grilled Chicken Salad", "Perfect for a light meal", R.drawable.avatar)
        ),
        "T" to listOf(
            Recommendation("Negative", "Mushroom Risotto", "Avoid heavy meals during this time", R.drawable.avatar),
            Recommendation("Positive", "Spaghetti Bolognese", "Good for evening meals", R.drawable.avatar)
        ),
        "W" to listOf(
            Recommendation("Positive", "Vegetarian Tacos", "Great for a quick lunch", R.drawable.avatar),
            Recommendation("Neutral", "Caesar Salad", "Great for a healthy meal", R.drawable.avatar)
        ),
        "T" to listOf(
            Recommendation("Negative", "Fried Chicken", "Not recommended for late night", R.drawable.avatar),
            Recommendation("Positive", "Grilled Salmon", "Perfect for dinner", R.drawable.avatar)
        ),
        "F" to listOf(
            Recommendation("Neutral", "Spaghetti Carbonara", "Best enjoyed with a glass of wine", R.drawable.avatar),
            Recommendation("Positive", "Salmon Sushi", "A healthy choice", R.drawable.avatar)
        ),
        "Su" to listOf(
            Recommendation("Neutral", "Vegetable Stir Fry", "Great for a weekend lunch", R.drawable.avatar),
            Recommendation("Positive", "Chicken Wrap", "Light and delicious", R.drawable.avatar)
        ),
        "Sa" to listOf(
            Recommendation("Negative", "Beef Stew", "Heavy meal, avoid before bedtime", R.drawable.avatar),
            Recommendation("Positive", "Quinoa Salad", "Light and healthy", R.drawable.avatar)
        )
    )

    fun loadRecommendationsForDay(day: String) {
        // Fetch the list of dishes for the selected day
        val dishesForDay = weeklyDishes[day] ?: emptyList()

        // Update LiveData or state to trigger UI update
        _recommendations.value = dishesForDay
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
    fun loadMoodFlowDataFromFirebase(userId: String, startDateMillis: Long, endDateMillis: Long) {
        getEntriesInDateRange(userId, startDateMillis, endDateMillis) { entries ->
            val scores = entries.map { it.second } // Extract the scores
            _moodFlowData.postValue(scores) // Update LiveData
        }
    }


}
