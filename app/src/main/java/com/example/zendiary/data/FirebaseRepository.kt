package com.example.zendiary.data

import android.util.Log
import com.example.zendiary.Global
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.zendiary.Global.userId
import com.example.zendiary.R
import com.example.zendiary.ui.analytics.models.DayPreview
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

object FirebaseRepository {

    fun fetchDayPreviewsForSearchQuery(
        query: String, // New parameter for search query
        callback: (List<DayPreview>) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val sentimentRef = database.getReference("users/$userId/entries") // Adjust path as needed

        sentimentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dayPreviews = mutableListOf<DayPreview>() // List to collect DayPreview entries

                for (child in snapshot.children) {
                    // Get the headerEntry and text
                    val headerEntry = child.child("headerEntry").value as? String ?: ""
                    val text = child.child("text").value as? String ?: ""

                    // Check if the query is contained in either headerEntry or text (case-insensitive)
                    if (headerEntry.contains(query, ignoreCase = true) || text.contains(query, ignoreCase = true)) {
                        // Extract the sentiment label, fallback to "Unknown" if not found
                        val result = child.child("sentiment").child("label").value as? String ?: "Unknown"

                        // Derive or map the mood icon resource ID based on the sentiment result
                        val moodIconResId = getMoodIconResId(result)

                        // Add a new DayPreview object to the list
                        dayPreviews.add(
                            DayPreview(
                                result = result,
                                title = headerEntry,
                                snippet = text,
                                moodIconResId = moodIconResId
                            )
                        )
                    }
                }
                // Pass all collected DayPreview objects to the callback
                callback(dayPreviews)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList()) // Handle errors by returning an empty list
            }
        })
    }

    fun fetchDayPreviewsForDate(
        dateIso: String,
        callback: (List<DayPreview>) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val sentimentRef = database.getReference("users/$userId/entries") // Adjust path as needed

        sentimentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Extract just the date part (yyyy-MM-dd) from the input date
                val targetDate = dateIso.substring(0, 10)
                val dayPreviews = mutableListOf<DayPreview>() // List to collect DayPreview entries

                for (child in snapshot.children) {
                    // Get the entry date
                    val entryDate = (child.child("date").value as? String)?.substring(0, 10)
                    if (entryDate == targetDate) {
                        // Extract the necessary fields
                        val result = child.child("sentiment").child("label").value as? String ?: "Unknown"
                        val title = child.child("headerEntry").value as? String ?: "Untitled"
                        val snippet = child.child("text").value as? String ?: "No content available"

                        // Derive or map the mood icon resource ID based on the sentiment result
                        val moodIconResId = getMoodIconResId(result)

                        // Add a new DayPreview object to the list
                        dayPreviews.add(
                            DayPreview(
                                result = result,
                                title = title,
                                snippet = snippet,
                                moodIconResId = moodIconResId
                            )
                        )
                    }
                }
                // Pass all collected DayPreview objects to the callback
                callback(dayPreviews)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList()) // Handle errors by returning an empty list
            }
        })
    }

    fun getMoodIconResId(sentiment: String): Int {
        return when (sentiment.lowercase()) {
            "positive" -> R.drawable.ic_sentiment_positive
            "negative" -> R.drawable.ic_sentiment_negative
            "neutral" -> R.drawable.ic_sentiment_neutral
            else -> R.drawable.ic_sentiment_neutral // Default icon
        }
    }

    fun getEntriesCountForUser(
        userId: String,
        callback: (Int) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val entriesRef = database.getReference("users/$userId/entries")

        entriesRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val count = dataSnapshot?.childrenCount?.toInt() ?: 0
                callback(count)
            } else {
                Log.e("FirebaseRepository", "Error fetching entries count", task.exception)
                callback(0) // Return 0 in case of error
            }
        }
    }

    fun getEntriesInDateRange(
        userId: String,
        startDateMillis: Long,
        endDateMillis: Long,
        callback: (List<Pair<String, Float>>) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val entriesRef = database.getReference("users/$userId/entries")

        // Convert milliseconds to ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val startDateIso = dateFormat.format(Date(startDateMillis))
        val endDateIso = dateFormat.format(Date(endDateMillis))

        entriesRef
            .get()
            .addOnSuccessListener { snapshot ->
                // Use a mutable list of Triple to include date, label, and score
                val entries = mutableListOf<Triple<String, String, Float>>()

                for (child in snapshot.children) {
                    val date = child.child("date").value as? String
                    val sentiment = child.child("sentiment")
                    val label = sentiment.child("label").value as? String
                    val score = (sentiment.child("score").value as? Number)?.toDouble()

                    // Include date in the entry if all values are valid
                    if (label != null && score != null && date != null && date >= startDateIso && date <= endDateIso) {
                        entries.add(Triple(date, label, score.toFloat()))
                    }
                }

                // Sort entries by date (ISO 8601 format allows lexicographical sorting)
                val sortedEntries = entries.sortedBy { it.first }

                // If you need only label and score, map it back to a list of Pair
                val result = sortedEntries.map { it.second to it.third }

                // Pass the sorted result to the callback
                callback(result)
            }
            .addOnFailureListener {
                callback(emptyList()) // Handle failure gracefully
            }
    }

    // Function to save a reminder option to Firebase
    fun saveReminderOption(
        reminderOption: Map<String, Any>,
        callback: (Boolean) -> Unit
    ) {
        val userId = Global.userId
        if (userId == null) {
            Log.e("FirebaseRepository", "User ID is null. Cannot save reminder option.")
            callback(false)
            return
        }

        val database = FirebaseDatabase.getInstance()
        val remindersRef = database.getReference("users/$userId/reminders/reminder_01")

        remindersRef.setValue(reminderOption)
            .addOnSuccessListener {
                // Update the Global object with the saved reminder data
                Global.reminderHour = reminderOption["hour"] as? Int ?: 0
                Global.reminderMinute = reminderOption["minute"] as? Int ?: 0
                Global.reminderRepeat = reminderOption["repeat"] as? String ?: "Never"
                Global.isReminderEnabled = reminderOption["isEnabled"] as? Boolean ?: false
                callback(true) // Successfully saved reminder
            }
            .addOnFailureListener {
                Log.e("FirebaseRepository", "Error saving reminder option", it)
                callback(false) // Failed to save reminder
            }
    }

    // Function to load all reminder options from Firebase
    fun loadReminderOptions(callback: (List<Map<String, Any>>) -> Unit) {
        val userId = Global.userId
        if (userId == null) {
            Log.e("FirebaseRepository", "User ID is null. Cannot load reminder options.")
            callback(emptyList())
            return
        }

        val database = FirebaseDatabase.getInstance()
        val remindersRef = database.getReference("users/$userId/reminders")

        remindersRef.get()
            .addOnSuccessListener { snapshot ->
                val reminderOptions = mutableListOf<Map<String, Any>>()

                for (child in snapshot.children) {
                    val reminder = child.value as? Map<String, Any>
                    if (reminder != null) {
                        reminderOptions.add(reminder)
                    }
                }

                // Update the Global object with the first reminder (or default values)
                if (reminderOptions.isNotEmpty()) {
                    val firstReminder = reminderOptions[0]
                    Global.reminderHour = (firstReminder["hour"] as? Long)?.toInt() ?: 0
                    Global.reminderMinute = (firstReminder["minute"] as? Long)?.toInt() ?: 0
                    Global.reminderRepeat = firstReminder["repeat"] as? String ?: "Never"
                    Global.isReminderEnabled = firstReminder["isEnabled"] as? Boolean ?: false
                } else {
                    Global.reminderHour = 0
                    Global.reminderMinute = 0
                    Global.reminderRepeat = "Never"
                    Global.isReminderEnabled = false
                }

                callback(reminderOptions) // Successfully loaded reminders
            }
            .addOnFailureListener {
                Log.e("FirebaseRepository", "Error loading reminder options", it)
                callback(emptyList()) // Failed to load reminders
            }
    }
}