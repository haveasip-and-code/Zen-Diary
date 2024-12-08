package com.example.zendiary.ui.home

data class Note(
    val header: String, // New header field
    val previewText: String, // Preview text for the note
    val date: String,        // Date of the note
    val imageUrl: String? = null // Optional image URL
)
