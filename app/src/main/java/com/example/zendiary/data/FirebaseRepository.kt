package com.example.zendiary.data

import android.util.Log
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

}