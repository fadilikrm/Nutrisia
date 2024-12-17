package com.example.nutrisia

import okhttp3.MultipartBody

data class ProfileRequest(
    val fullname: String?,
    val username: String?,
    val email: String?,
    val password: String?,
    val tb: Float?,
    val bb: Float?,
    val jk: String?,
    val age: Int?,
    val aktivitas: String?,
    val foto: MultipartBody.Part? // Gunakan untuk file foto
)

