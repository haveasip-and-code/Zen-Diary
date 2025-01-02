package com.example.zendiary.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zendiary.data.FirebaseRepository.fetchDayPreviewsForDate
import com.example.zendiary.data.FirebaseRepository.getEntriesInDateRange
import com.example.zendiary.ui.analytics.models.DayPreview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsViewModel : ViewModel() {
    // LiveData for selected day/week toggle state
    private val _isDayViewSelected = MutableLiveData(true) // Default to "Day"
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

    private val _weeklyDayPreviews = MutableLiveData<List<Pair<String, List<DayPreview>>>>()
    private val weeklyDayPreviews: LiveData<List<Pair<String, List<DayPreview>>>> get() = _weeklyDayPreviews

    private val _dayPreviews = MutableLiveData<List<DayPreview>>()
    val dayPreviews: LiveData<List<DayPreview>> get() = _dayPreviews

    init {
        // Load initial data
        loadMoodFlowData()
        initializeWeekDays()

        val calendar = Calendar.getInstance()
        // Get the current day of the week
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ..., Saturday = 7
        when (currentDayOfWeek) {
            1 -> updateSelectedDay("Su")
            2 -> updateSelectedDay("M")
            3 -> updateSelectedDay("Tu")
            4 -> updateSelectedDay("W")
            5 -> updateSelectedDay("Th")
            6 -> updateSelectedDay("F")
            7 -> updateSelectedDay("Sa")
        }

        loadPreviewsForDay(selectedDay.value ?: "M")
    }

    private fun initializeWeekDays() {
        val calendar = Calendar.getInstance()
        // Set the first day of the week to Monday (if not already set)
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Set to the start of the week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        // Set to the end of the week (Sunday at 23:59:59)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.timeInMillis

        // Update LiveData with the date range
        _dateRange.value = startOfWeek to endOfWeek

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ..., Saturday = 7

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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        // Fetch sentiment and initialize menu for each day
        val dayAbbreviations = listOf("M", "Tu", "W", "Th", "F", "Sa", "Su")

        val dayPreviewsList = mutableListOf<Pair<String, List<DayPreview>>>()

        // Add the days of the week starting from Monday
        for (i in 0 until 7) {
            val dayOfMonth = dayOfMonthFormat.format(calendar.time)

            // Get the weekday abbreviation
            val weekday = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "Tu"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "Th"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "Sa"
                Calendar.SUNDAY -> "Su"
                else -> ""
            }

            // Add to the week days list
            weekDays.add("$weekday $dayOfMonth")

            // Get the date in ISO format for database queries
            val currentDateIso = dateFormat.format(calendar.time)

            // Fetch day previews for the current date
            fetchDayPreviewsForDate(currentDateIso) { dayPreviews ->
                // If day previews are found for the date
                if (dayPreviews.isNotEmpty()) {
                    // Add the day's abbreviation and its corresponding previews to the list
                    dayPreviewsList.add(dayAbbreviations[i] to dayPreviews)
                } else {
                    // If no previews are found, add an empty list for the day's abbreviation
                    dayPreviewsList.add(dayAbbreviations[i] to emptyList())
                }

                // If this is the last day of the week, update the LiveData or variable
                if (dayPreviewsList.size == dayAbbreviations.size) {
                    _weeklyDayPreviews.value = dayPreviewsList
                    loadPreviewsForDay(selectedDay.value ?: "M")
                }
            }

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the value in the LiveData or equivalent variable
        _weekDays.value = weekDays
    }


    fun loadPreviewsForDay(day: String) {
        // Fetch the list of dishes for the selected day
        val previewsForDay = weeklyDayPreviews.value?.firstOrNull { it.first == day }?.second ?: emptyList()

        // Update LiveData or state to trigger UI update
        _dayPreviews.value = previewsForDay
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

    // Method to reset to default state
    fun resetDayView() {
        _isDayViewSelected.value = true
        initializeWeekDays()
    }
}
