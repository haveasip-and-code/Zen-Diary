package com.example.zendiary

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.View
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LogIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Setting up window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // "Sign in" button listener to switch to sign_in.xml layout
        val signInButton = findViewById<ImageButton>(R.id.imageButton)
        signInButton.setOnClickListener {
            setContentView(R.layout.sign_in)
        }

        // "Sign up" button listener to switch to sign_up.xml layout
        val signUpButton = findViewById<ImageButton>(R.id.imageButton2)
        signUpButton.setOnClickListener {
            setContentView(R.layout.sign_up)
        }

        // Back button listener to switch to fragment_reminder and configure its views
        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            setContentView(R.layout.fragment_reminder) // Switch to fragment_reminder layout
            configureReminderFragmentViews() // Set up views specific to fragment_reminder
        }
    }

    private fun configureReminderFragmentViews() {
        // Initialize and configure setTime TextView and NumberPicker within fragment_reminder
        val hourPicker = findViewById<NumberPicker>(R.id.hour_picker)
        val minPicker = findViewById<NumberPicker>(R.id.minute_picker)

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
        val repeatTextView = findViewById<TextView>(R.id.repeatTextView)
        repeatTextView.setOnClickListener {
            showPopupMenu(repeatTextView)  // Show the PopupMenu when clicked
        }
    }

    private fun showPopupMenu(view: View) {
        val wrapper = ContextThemeWrapper(this, R.style.CustomPopupMenu)
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
