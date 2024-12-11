package com.example.nutrisia

data class InsertProfile(
    val user_id: Int,  // Tambahkan user_id
    val tb: Float,
    val bb: Float,
    val jk: String,
    val age: Int,
    val aktivitas: String,
    val status: Boolean? = null
)


