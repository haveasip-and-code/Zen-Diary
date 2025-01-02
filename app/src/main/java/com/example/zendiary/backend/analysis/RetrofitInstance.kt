package com.example.zendiary.backend.analysis

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://harryduong.pythonanywhere.com") // Flask API URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SentimentAnalysisApi = retrofit.create(SentimentAnalysisApi::class.java)
}

