package com.example.nutrisia

data class UserNutrisia(
    val fullname: String,
    val username: String,
    val email: String,
    val password: String,
    val status: Boolean? = null
)
