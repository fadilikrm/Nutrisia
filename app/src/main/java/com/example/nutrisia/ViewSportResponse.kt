package com.example.nutrisia

data class ViewSportResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<ViewSport>? = null
)
