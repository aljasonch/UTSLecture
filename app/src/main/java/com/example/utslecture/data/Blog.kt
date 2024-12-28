package com.example.utslecture.data

import java.util.Date

data class Blog(
    val blogId: String = "",
    val userId: String = "",
    val title: String = "",
    val image: String = "",
    val content: String = "",
    val views: Int = 0,
    val likes: Int = 0,
    val username: String = "",
    val uploadDate: Date? = null,
    val category: String = "",
    var viewMilestoneNotified: Boolean = false,
    var likeMilestoneNotified: Boolean = false
)