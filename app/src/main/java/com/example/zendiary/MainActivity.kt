package com.example.zendiary

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.zendiary.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define the fragments where the BottomNavigationView should be hidden
        val fragmentsToHideBottomNav = setOf(
            R.id.journalFragment
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

        // Add a listener to show/hide BottomNavigationView based on the destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fragmentsToHideBottomNav) {
                navView.visibility = View.GONE
                binding.journalButton.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
                binding.journalButton.visibility = View.VISIBLE
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        val navViewDrawer: NavigationView = findViewById(R.id.nav_view_drawer)
        navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}