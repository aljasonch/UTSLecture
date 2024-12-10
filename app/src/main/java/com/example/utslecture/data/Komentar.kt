package com.example.utslecture.data

import java.security.Timestamp
import java.util.Date

data class Komentar(
    val userId: String = "",
    val blogId: String = "",
    val content: String = "",
    val username: String = "",
    val uploadDate: Date = Date(),
    val uploadDateString: String = ""
)