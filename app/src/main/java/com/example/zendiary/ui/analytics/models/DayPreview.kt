package com.example.zendiary.ui.analytics.models

data class DayPreview(
    val result: String,           // The sentiment result of the journal entry
    val title: String,          // Title of the journal entry
    val snippet: String,        // A snippet or preview of the journal content
    val moodIconResId: Int      // Resource ID for the mood icon associated with the entry
)
