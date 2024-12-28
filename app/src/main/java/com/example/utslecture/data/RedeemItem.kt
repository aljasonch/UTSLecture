package com.example.utslecture.data

data class RedeemItem(
    val id: String,
    val name: String,
    val points: Int,
    val imageUrl: String,
    val redeemCode: String? = null
)