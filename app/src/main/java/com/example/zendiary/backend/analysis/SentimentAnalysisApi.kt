package com.example.zendiary.backend.analysis

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SentimentAnalysisApi {
    @POST("/analyze_sentiment")
    suspend fun analyzeSentiment(@Body text: Map<String, String>): Response<Map<String, Float>>
}

