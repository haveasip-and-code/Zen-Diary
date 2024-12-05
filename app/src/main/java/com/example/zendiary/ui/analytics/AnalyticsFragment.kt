package com.example.zendiary.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zendiary.R
import com.example.zendiary.databinding.FragmentAnalyticsBinding
import com.example.zendiary.ui.analytics.adapters.RecommendationsAdapter
import com.example.zendiary.ui.analytics.adapters.WeekDaysAdapter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    private lateinit var weekDaysAdapter: WeekDaysAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter

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
        setupRecommendationsRecyclerView()

        setupDayWeekToggle()

        setupCalendar()

        observeViewModel()
    }

    private fun setupWeekDaysRecyclerView() {
        weekDaysAdapter = WeekDaysAdapter { selectedDay ->
            viewModel.updateSelectedDay(selectedDay) // Notify ViewModel when a day is selected
        }
        binding.rvWeekDays.apply {
            adapter = weekDaysAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupRecommendationsRecyclerView() {
        recommendationsAdapter = RecommendationsAdapter()
        binding.rvRecommendations.apply {
            adapter = recommendationsAdapter
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

                // Create a date range picker
                val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now()) // Disallow past dates

                val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select Date Range")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

                datePicker.show(parentFragmentManager, "DATE_PICKER")

                // Listen to the selected date range
                datePicker.addOnPositiveButtonClickListener { dateRange ->
                    val startDate = dateRange.first // Start date in milliseconds
                    val endDate = dateRange.second // End date in milliseconds

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
        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            recommendationsAdapter.submitList(recommendations)
        }

        // Observe day/week toggle state
        viewModel.isDayViewSelected.observe(viewLifecycleOwner) { isDayMode ->
            binding.rvWeekDays.visibility = if (isDayMode) View.VISIBLE else View.GONE
            binding.rvRecommendations.visibility = if (isDayMode) View.VISIBLE else View.GONE
            binding.tvSelectedWeek.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.btnNextWeek.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.btnPreviousWeek.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.tvmoodflowtitle.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.lineChartMoodFlow.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.tvmoodbartitle.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.llMoodBarContainer.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.llMoodBarDesContainer.visibility = if (!isDayMode) View.VISIBLE else View.GONE
            binding.btnDay.isSelected = isDayMode
            binding.btnWeek.isSelected = !isDayMode
        }

        viewModel.dateRange.observe(viewLifecycleOwner) { range ->
            val (start, end) = range
            // Format and display the date range, or perform analytics filtering
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
