package com.example.zendiary.ui.home.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Notification(val title: String, val message: String, val date: String)

class NotificationViewModel : ViewModel() {

    // Sample list of notifications
    private val _notifications = MutableLiveData<List<Notification>>().apply {
        value = listOf(
            Notification("Reminder", "Don't forget to journal today!", "2024-11-03"),
            Notification("New Feature", "Check out the new mood tracking feature!", "2024-11-02")
        )
    }

    val notifications: LiveData<List<Notification>> = _notifications
}