package com.example.nutrisia

data class InsertSport(
    val user_id: Int,
    val jenis_olahraga: String,
    val durasi: Int,
    val status: Boolean? = null
)
