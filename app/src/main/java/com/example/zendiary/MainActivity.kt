package com.example.zendiary

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.zendiary.data.FirebaseRepository
import com.example.zendiary.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.example.zendiary.ui.profile.StoreFragment
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val reminders = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Chaquopy (only needed once per app)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // Run the Python Flask app in a background thread
        val python = Python.getInstance()
        val pyObject = python.getModule("app")

        // Use an Executor to run the Flask server in the background
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                pyObject.callAttr("run")
                Log.d("MainActivity", "Flask server started successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting Flask server: ${e.message}")
            }
        }


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Load reminders at app startup
        loadReminders()

        // Define the fragments where the BottomNavigationView should be hidden
        val fragmentsToHideBottomNav = setOf(
            R.id.journalFragment,
            R.id.serviceFragment,
            R.id.editProfileFragment,
            R.id.reminderTime,
            R.id.pincode,
            R.id.deletionConfirmationFragment,
            R.id.paymentFragment,
            R.id.storeFragment
        )

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_analytics,
                R.id.navigation_explore,
                R.id.navigation_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Hide the action bar
        supportActionBar?.hide()

        // Set up click listener for the journal button
        binding.journalButton.setOnClickListener {
            navController.navigate(R.id.journalFragment)
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        val navViewDrawer: NavigationView = findViewById(R.id.nav_view_drawer)
        navViewDrawer.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//
//            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Add a listener to show/hide BottomNavigationView based on the destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fragmentsToHideBottomNav) {
                navView.visibility = View.GONE
                binding.journalButton.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
                binding.journalButton.visibility = View.VISIBLE
            }
            when (destination.id) {
                R.id.journalFragment -> {
                    // Enable the drawer for JournalFragment
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
                else -> {
                    // Disable the drawer for other fragments
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }
    }

    private fun loadReminders() {
        FirebaseRepository.loadReminderOptions { loadedReminders ->
            if (loadedReminders.isNotEmpty()) {
                reminders.clear()
                reminders.addAll(loadedReminders)
                Log.d("MainActivity", "Loaded ${reminders.size} reminders.")
            } else {
                Log.d("MainActivity", "No reminders found at startup.")
            }
        }
    }

    private fun saveReminders() {
        reminders.forEachIndexed { _, reminder ->
            FirebaseRepository.saveReminderOption(reminder) { success ->
                if (!success) {
                    Log.e("MainActivity", "Failed to save reminder")
                }
            }
        }
        Log.d("MainActivity", "All reminders saved before shutdown.")
    }

    override fun onDestroy() {
        super.onDestroy()
        saveReminders() // Save reminders when the activity is destroyed
    }
}