package com.example.zendiary.ui.profile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zendiary.Global
import com.example.zendiary.R
import com.example.zendiary.backend.reminder.ReminderBroadcastReceiver
import com.example.zendiary.data.FirebaseRepository.saveReminderOption
import java.util.Calendar

class ReminderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for fragment
        val rootView = inflater.inflate(R.layout.fragment_reminder, container, false)

        // Set up the "Back" button
        val backButton: ImageButton = rootView.findViewById(R.id.back_button_reminder)
        backButton.setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().navigateUp()
        }

        // Configure the fragment views
        configureReminderFragmentViews(rootView)

        return rootView
    }

    private fun configureReminderFragmentViews(rootView: View) {
        val hourPicker = rootView.findViewById<NumberPicker>(R.id.hour_picker)
        val minPicker = rootView.findViewById<NumberPicker>(R.id.minute_picker)
        val repeatTextView = rootView.findViewById<TextView>(R.id.repeatTextView)
        val reminderSwitch = rootView.findViewById<SwitchCompat>(R.id.reminder_switch)
        val submitButton = rootView.findViewById<ImageButton>(R.id.submit_button_reminder)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        minPicker.minValue = 0
        minPicker.maxValue = 59

        // Load settings from Global
        hourPicker.value = Global.reminderHour
        minPicker.value = Global.reminderMinute
        repeatTextView.text = Global.reminderRepeat
        reminderSwitch.isChecked = Global.isReminderEnabled

        repeatTextView.setOnClickListener {
            showPopupMenu(repeatTextView)
        }

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            Global.isReminderEnabled = isChecked
            if (!isChecked) cancelReminder() // Turn off the reminder
        }

        submitButton.setOnClickListener {
            // Save settings to Global
            Global.reminderHour = hourPicker.value
            Global.reminderMinute = minPicker.value
            Global.reminderRepeat = repeatTextView.text.toString()

            // Save the reminder settings to Firebase (or any other storage mechanism you are using)
            val reminderOptions = mapOf(
                "hour" to Global.reminderHour,
                "minute" to Global.reminderMinute,
                "repeat" to Global.reminderRepeat,
                "isEnabled" to Global.isReminderEnabled
            )

            // Save the reminder options using Firebase
            saveReminderOption(reminderOptions) { success ->
                if (success) {
                    // Show a toast message
                    Toast.makeText(requireContext(), "Reminder Saved!", Toast.LENGTH_SHORT).show()

                    // Schedule the reminder if enabled
                    if (Global.isReminderEnabled) {
                        if (Global.reminderRepeat == "Never") {
                            scheduleSingleDayReminder()
                        } else {
                            scheduleRepeatingReminder(
                                Global.reminderHour,
                                Global.reminderMinute,
                                Global.reminderRepeat
                            )
                        }
                    }

                    // Navigate back to the previous screen
                    findNavController().navigateUp()
                } else {
                    // Show a toast if saving failed
                    Toast.makeText(requireContext(), "Failed to save reminder!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun showPopupMenu(view: View) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu)
        val popupMenu = PopupMenu(wrapper, view)

        // Inflate the menu resource file
        popupMenu.menuInflater.inflate(R.menu.repeat_menu_reminder, popupMenu.menu)

        // Set a listener to handle item clicks
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.every_monday -> setRepeatDay(view as TextView, "Every Monday")
                R.id.every_tuesday -> setRepeatDay(view as TextView, "Every Tuesday")
                R.id.every_wednesday -> setRepeatDay(view as TextView, "Every Wednesday")
                R.id.every_thursday -> setRepeatDay(view as TextView, "Every Thursday")
                R.id.every_friday -> setRepeatDay(view as TextView, "Every Friday")
                R.id.every_saturday -> setRepeatDay(view as TextView, "Every Saturday")
                R.id.every_sunday -> setRepeatDay(view as TextView, "Every Sunday")
                R.id.never -> {
                    (view as TextView).text = "Never"
                    scheduleSingleDayReminder()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    private fun setRepeatDay(textView: TextView, day: String): Boolean {
        textView.text = day
        if (Global.isReminderEnabled) {
            val hour = getHourFromPicker()
            val minute = getMinuteFromPicker()
            scheduleRepeatingReminder(hour, minute, day)
        }
        return true
    }

    private fun scheduleSingleDayReminder() {
        if (!Global.isReminderEnabled) return

        val hour = getHourFromPicker()
        val minute = getMinuteFromPicker()

        requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.DATE, 1) // Set for the following day
        }

        context?.let { scheduleExactAlarm(it, calendar, pendingIntent) }
    }

    private fun scheduleExactAlarm(context: Context, calendar: Calendar, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } catch (e: SecurityException) {
                    Log.e("AlarmManager", "Failed to set exact alarm: ${e.message}")
                }
            } else {
                // Navigate to settings to grant the permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } else {
            // For devices below Android 12
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun scheduleRepeatingReminder(hour: Int, minute: Int, repeatDay: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val daysOfWeek = mapOf(
            "Every Monday" to Calendar.MONDAY,
            "Every Tuesday" to Calendar.TUESDAY,
            "Every Wednesday" to Calendar.WEDNESDAY,
            "Every Thursday" to Calendar.THURSDAY,
            "Every Friday" to Calendar.FRIDAY,
            "Every Saturday" to Calendar.SATURDAY,
            "Every Sunday" to Calendar.SUNDAY
        )

        daysOfWeek[repeatDay]?.let { targetDay ->
            while (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
                calendar.add(Calendar.DATE, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7, // Weekly interval
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun getHourFromPicker(): Int {
        val hourPicker = requireView().findViewById<NumberPicker>(R.id.hour_picker)
        return hourPicker.value
    }

    private fun getMinuteFromPicker(): Int {
        val minPicker = requireView().findViewById<NumberPicker>(R.id.minute_picker)
        return minPicker.value
    }
}
