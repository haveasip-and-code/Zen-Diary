package com.example.zendiary.ui.profile

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zendiary.R

class ReminderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho fragment
        val rootView = inflater.inflate(R.layout.fragment_reminder, container, false)

        // Thiết lập sự kiện cho nút "Back"
        val backButton: ImageButton = rootView.findViewById(R.id.back_button_reminder)
        backButton.setOnClickListener {
            // Sử dụng NavController để quay lại ProfileFragment
            findNavController().navigateUp() // Điều này sẽ đưa bạn quay lại fragment trước đó
        }

        // Cấu hình các view của fragment_reminder
        configureReminderFragmentViews(rootView)

        return rootView
    }

    private fun configureReminderFragmentViews(rootView: View) {
        // Initialize and configure setTime TextView and NumberPicker within fragment_reminder
        val hourPicker = rootView.findViewById<NumberPicker>(R.id.hour_picker)
        val minPicker = rootView.findViewById<NumberPicker>(R.id.minute_picker)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        minPicker.minValue = 0
        minPicker.maxValue = 59

        // NumberPicker value change listener
        hourPicker.setOnValueChangedListener { _, oldVal, newVal ->
            onNumberPickerListener(oldVal, newVal)
        }

        minPicker.setOnValueChangedListener { _, oldVal, newVal ->
            onNumberPickerListener(oldVal, newVal)
        }

        // Add a click listener for repeatTextView to show the PopupMenu
        val repeatTextView = rootView.findViewById<TextView>(R.id.repeatTextView)
        repeatTextView.setOnClickListener {
            showPopupMenu(repeatTextView)  // Show the PopupMenu when clicked
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
                R.id.every_monday -> {
                    (view as TextView).text = "Every Monday"
                    true
                }
                R.id.every_tuesday -> {
                    (view as TextView).text = "Every Tuesday"
                    true
                }
                R.id.every_wednesday -> {
                    (view as TextView).text = "Every Wednesday"
                    true
                }
                R.id.every_thursday -> {
                    (view as TextView).text = "Every Thursday"
                    true
                }
                R.id.every_friday -> {
                    (view as TextView).text = "Every Friday"
                    true
                }
                R.id.every_saturday -> {
                    (view as TextView).text = "Every Saturday"
                    true
                }
                R.id.every_sunday -> {
                    (view as TextView).text = "Every Sunday"
                    true
                }
                R.id.never -> {
                    (view as TextView).text = "Never"
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    private fun onNumberPickerListener(oldValue: Int, newValue: Int) {
        // Define behavior for value change here
    }
}
