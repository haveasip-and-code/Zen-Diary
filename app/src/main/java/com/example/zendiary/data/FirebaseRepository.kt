package com.example.zendiary.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FirebaseRepository {

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

    fun getSentimentFromFirebase(
        userId: String,
        entryId: String,
        callback: (String?, Float?) -> Unit)
    {
        val database = FirebaseDatabase.getInstance()
        val sentimentRef = database.getReference("users/$userId/entries/$entryId/sentiment")

        sentimentRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val label = dataSnapshot.child("label").getValue(String::class.java)
                val score = dataSnapshot.child("score").getValue(Float::class.java)
                callback(label, score)
            } else {
                // Handle failure
                callback(null, null)
            }
        }
    }

    fun convertIsoToDate(isoDate: String): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = isoFormat.parse(isoDate)

        // Convert to yyyy-MM-dd format
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(date ?: Date())
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
        val endDateIso = dateFormat.format(Date(endDateMillis+ (24 * 60 * 60 * 1000)))

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