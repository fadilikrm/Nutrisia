package com.example.nutrisia

data class ViewProgramResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<ViewProgram> = emptyList()
)
