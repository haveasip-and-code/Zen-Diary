package com.example.zendiary.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ContextThemeWrapper
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zendiary.MainActivity
import com.example.zendiary.R

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


        // "Sign in" button listener to switch to MainActivity


        // "Sign in" button listener to switch to sign_in.xml layout
        val signInButton = findViewById<ImageButton>(R.id.imageButton)
        signInButton.setOnClickListener {
            setContentView(R.layout.sign_in)

            val homeButton = findViewById<ImageButton>(R.id.toHome)
            homeButton.setOnClickListener {
                // Start MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // "Sign up" button listener to switch to sign_up.xml layout
        val signUpButton = findViewById<ImageButton>(R.id.imageButton2)
        signUpButton.setOnClickListener {
            setContentView(R.layout.sign_up)
        }

    }
}
