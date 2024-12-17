package com.example.nutrisia

data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LoginUserData? = null
)