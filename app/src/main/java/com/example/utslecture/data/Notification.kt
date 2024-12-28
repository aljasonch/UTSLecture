package com.example.utslecture.data

import java.util.Date

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Date = Date()
)