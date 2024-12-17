package com.example.nutrisia

data class SelectProfileResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<SelectProfile> = emptyList()
)
