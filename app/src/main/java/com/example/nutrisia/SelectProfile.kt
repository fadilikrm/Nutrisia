package com.example.nutrisia

data class SelectProfile(
    val fullname: String = "",
    val username: String = "",
    val email: String = "",
    val foto: String ="",
    val tb: Double = 0.0,
    val bb: Double = 0.0,
    val password: String = "",
    val aktivitas: String = "",
    val jk: String = "",
    val age: Int = 0
)
