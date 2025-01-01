package com.example.zendiary.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zendiary.Global.userId
import com.example.zendiary.R
import com.example.zendiary.databinding.FragmentAnalyticsBinding
import com.example.zendiary.ui.analytics.adapters.DayPreviewAdapter
import com.example.zendiary.ui.analytics.adapters.WeekDaysAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    private lateinit var weekDaysAdapter: WeekDaysAdapter
    private lateinit var dayPreviewAdapter: DayPreviewAdapter

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        binding.ibCalendar.isEnabled = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWeekDaysRecyclerView()
        setupDayPreviewRecyclerView()

        setupDayWeekToggle()

        setupCalendar()

        observeViewModel()
    }

    private fun setupChart(moodScores: List<Float>) {
        // Initialize the LineChart
        lineChart = binding.lineChartMoodFlow

        // Convert the moodScores list to chart entries
        val entries = moodScores.mapIndexed { index, score -> Entry(index.toFloat(), score) }

        // Create the dataset
        val dataSet = LineDataSet(entries, "Mood Flow")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.RED) // Circle color
        dataSet.circleRadius = 4f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curved line

        // Bind data to the chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize the chart
        lineChart.axisLeft.axisMinimum = -2f // Minimum Y-axis value
        lineChart.axisLeft.axisMaximum = 2f // Maximum Y-axis value
        lineChart.axisRight.isEnabled = false // Disable the right Y-axis
        lineChart.xAxis.granularity = 1f // Only whole numbers for X-axis
        lineChart.description.isEnabled = false // Disable description
        lineChart.invalidate() // Refresh the chart
    }

    private fun setupWeekDaysRecyclerView() {
        weekDaysAdapter = WeekDaysAdapter { selectedDay ->
            viewModel.updateSelectedDay(selectedDay) // Notify ViewModel when a day is selected
            viewModel.loadPreviewsForDay(selectedDay.split(" ")[0])
        }
        binding.rvWeekDays.apply {
            adapter = weekDaysAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDayPreviewRecyclerView() {
        dayPreviewAdapter = DayPreviewAdapter()
        binding.rvDayPreview.apply {
            adapter = dayPreviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupDayWeekToggle() {
        // Get references to the buttons
        val btnDay = binding.btnDay
        val btnWeek = binding.btnWeek

        // Set initial states (Day selected by default)
        btnDay.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.selected_round_button))
        btnDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        btnWeek.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.transparent))
        btnWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_home_text))

        // Click listeners for toggling
        btnDay.setOnClickListener {
            // Update "Day" button to selected state
            btnDay.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.selected_round_button))
            btnDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            // Update "Week" button to unselected state
            btnWeek.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.transparent))
            btnWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_home_text))

            binding.ibCalendar.isEnabled = false
            viewModel.toggleDayWeekView(true)
        }

        btnWeek.setOnClickListener {
            // Update "Week" button to selected state
            btnWeek.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.selected_round_button))
            btnWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            // Update "Day" button to unselected state
            btnDay.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.transparent))
            btnDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_home_text))

            binding.ibCalendar.isEnabled = true
            viewModel.toggleDayWeekView(false)
        }
    }

    private fun setupCalendar() {
        binding.ibCalendar.setOnClickListener {
            if (binding.ibCalendar.isEnabled) {

                // Get the current date in milliseconds
                val currentDateMillis = System.currentTimeMillis()

                // Create a calendar constraint to allow only dates before today
                val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.before(currentDateMillis)) // Disallow future dates

                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select Date Range")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

                datePicker.show(parentFragmentManager, "DATE_PICKER")

                // Listen to the selected date range
                datePicker.addOnPositiveButtonClickListener { dateRange ->
                    val startDate = dateRange.first // Start date in milliseconds

                    // Adjust endDate to the end of the selected day (23:59:59.999)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dateRange.second
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val endDate = calendar.timeInMillis

                    // Perform actions based on the selected date range
                    viewModel.setDateRange(startDate, endDate)
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe the week days list
        viewModel.weekDays.observe(viewLifecycleOwner) { days ->
            weekDaysAdapter.submitList(days)
        }

        // Observe recommendations list
        viewModel.dayPreviews.observe(viewLifecycleOwner) { previews ->
            dayPreviewAdapter.submitList(previews)
        }

        // Observe day/week toggle state
        viewModel.isDayViewSelected.observe(viewLifecycleOwner) { isDayMode ->
            binding.rvWeekDays.visibility = if (isDayMode) View.VISIBLE else View.GONE
            binding.rvDayPreview.visibility = if (isDayMode) View.VISIBLE else View.GONE
            binding.tvRecommendations.visibility = if (isDayMode) View.GONE else View.GONE
            binding.tvSelectedWeek.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.btnNextWeek.visibility = if (!isDayMode) View.GONE else View.GONE
            binding.btnPreviousWeek.visibility = if (!isDayMode) View.GONE else View.GONE
            binding.tvmoodflowtitle.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.lineChartMoodFlow.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.tvmoodbartitle.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.llMoodBarContainer.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.llMoodBarDesContainer.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.btnDay.isSelected = isDayMode
            binding.btnWeek.isSelected = !isDayMode
        }

        // Observe date range LiveData
        viewModel.dateRange.observe(viewLifecycleOwner) { range ->
            val (start, end) = range
            range?.let {
                val formattedRange = formatDateRange(it.first, it.second)
                binding.tvSelectedWeek.text = formattedRange
            }
            userId?.let { viewModel.loadMoodFlowDataFromFirebase(it, start, end) }
        }

        // Observe the mood flow data
        viewModel.moodFlowData.observe(viewLifecycleOwner) { moodScores ->
            setupChart(moodScores)
        }
    }

    // Helper function to format date range
    private fun formatDateRange(startMillis: Long, endMillis: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = dateFormat.format(Date(startMillis))
        val endDate = dateFormat.format(Date(endMillis))
        return "$startDate - $endDate"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Reset the toggle state to "Day" when the fragment becomes visible
        viewModel.resetDayView()
    }

    override fun onPause() {
        super.onPause()
        // Reset the DayPreviewAdapter
        dayPreviewAdapter.submitList(emptyList()) // Clear the list
    }

}
