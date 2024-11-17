package com.example.zendiary.backend.analysis

data class SentimentResponse(
    val sentiment: String,  // Example: "positive", "neutral", "negative"
    val confidence: Double  // Example: 0.85 for 85% confidence
)
