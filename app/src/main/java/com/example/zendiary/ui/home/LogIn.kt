package com.example.zendiary.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zendiary.R
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zendiary.MainActivity
import com.example.zendiary.Global

class LogIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val signInButton = findViewById<ImageButton>(R.id.signinButton)
        signInButton.setOnClickListener {
            setContentView(R.layout.sign_in)

            val emailField = findViewById<EditText>(R.id.editTextEmailsigin) // EditText nhập email
            val passwordField = findViewById<EditText>(R.id.editTextPasswordsigni) // EditText nhập mật khẩu
            val homeButton = findViewById<ImageButton>(R.id.toHome)

            homeButton.setOnClickListener {
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Email và mật khẩu không được để trống", Toast.LENGTH_SHORT).show()
                } else {
                    checkUserInDatabase(email, password) { isValid ->
                        if (isValid) {
                            if (Global.userId != null) {
                                Log.d("Login", "Đăng nhập thành công! User ID: ${Global.userId}")
                            } else {
                                Log.d("Login", "Đăng nhập thành công, nhưng không lấy được User ID.")
                            }

                            // Chuyển sang MainActivity nếu thông tin đúng
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // Hiển thị lỗi nếu thông tin không đúng
                            Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


        // "Sign up" button listener to switch to sign_up.xml layout
        val signUpButton = findViewById<ImageButton>(R.id.signupButton)
        signUpButton.setOnClickListener {
            setContentView(R.layout.sign_up)

            // Handle sign-up form submission
            val submitButton = findViewById<ImageButton>(R.id.sign_up_button)
            submitButton.setOnClickListener {
                val fullname = findViewById<EditText>(R.id.editName).text.toString().trim()
                val email = findViewById<EditText>(R.id.editTextEmail).text.toString().trim()
                val phone = findViewById<EditText>(R.id.editTextPhone).text.toString().trim()
                val password = findViewById<EditText>(R.id.editTextPassword).text.toString().trim()
                val confirmPassword = findViewById<EditText>(R.id.editConfirmPassword).text.toString().trim()

                if (fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveUserToFirebase(fullname, email, phone, password)
            }
        }
    }

    private fun saveUserToFirebase(fullname: String, email: String, phone: String, password: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // Generate random userId
        val newUserId = "userId_" + (1..10).map { (0..9).random() }.joinToString("")

        // User data to be saved
        val userData = mapOf(
            "profile" to mapOf(
                "fullname" to fullname,
                "email" to email,
                "phone" to phone,
                "passwordHash" to password // You may hash this in production
            ),
            "balance" to 0,
            "reminders" to emptyMap<String, Any>(),
            "entries" to emptyMap<String, Any>(),
            "store" to mapOf(
                "purchasedStickers" to emptyList<String>(),
                "purchasedThemes" to emptyList<String>()
            ),
            "transactions" to emptyMap<String, Any>(),
            "trends" to emptyMap<String, Any>()
        )

        // Save to Firebase
        usersRef.child(newUserId).setValue(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                // Optionally navigate back to the login page or another screen
                recreate()
            } else {
                Toast.makeText(this, "Failed to register user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkUserInDatabase(email: String, password: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot.exists()) {
                    var isValidUser = false

                    for (userSnapshot in snapshot.children) {
                        val dbEmail = userSnapshot.child("profile").child("email").value.toString()
                        val dbPassword = userSnapshot.child("profile").child("passwordHash").value.toString()

                        if (dbEmail == email && dbPassword == password) {
                            isValidUser = true
                            val foundUserId = userSnapshot.key // Lấy giá trị userId
                            if (foundUserId != null) {
                                Global.userId = foundUserId // Update Global.userId nếu userId hợp lệ
                            }
                            break
                        }
                    }

                    callback(isValidUser) // Trả về kết quả
                } else {
                    callback(false)
                }
            } else {
                callback(false)
            }
        }
    }

}
