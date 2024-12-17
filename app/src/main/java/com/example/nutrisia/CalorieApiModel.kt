package com.example.nutrisia

data class CalorieRequest(
    val user_id: Int,
    val tb: Int,
    val bb: Int,
    val jk: String,
    val aktivitas: String,
    val age: Int
)

data class CalorieResponse(
    val status: Boolean,
    val message: String,
    val data: CalorieData?
)

data class CalorieData(
    val jml_kal: Double
)

data class CalorieInfo(
    val first: Int,
    val second: Int,
    val third: Int,
)

data class ScanCalorieRequest(
    val user_id: Int,
    val total_calories: Double,
    val scan_date: String
)

data class ApiResponse(
    val status: Boolean,
    val message: String,
    val data: CalorieData? // Data bagian ini bisa null jika tidak ada data yang dikembalikan
)
