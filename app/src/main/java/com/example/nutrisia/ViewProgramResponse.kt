package com.example.nutrisia

data class ViewProgramResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: ViewProgram? = null // Ubah dari List<ViewProgram> menjadi ViewProgram
)
