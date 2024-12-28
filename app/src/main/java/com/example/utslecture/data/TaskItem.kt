package com.example.utslecture.data

data class TaskItem(
    val id: String,
    val title: String,
    val points: Int,
    val isClaimed: Boolean = false,
    val requirementType: String? = null, // Make it nullable
    val requirementValue: Int? = null, // Make it nullable
    val currentProgress: Int? = null // Add current progress
)