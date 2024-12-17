package com.example.nutrisia

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.Response
import com.example.nutrisia.ScanCalorieRequest

interface ApiService {
    @POST("api/kelompok_1/register.php")
    fun addStudent(@Body student: UserNutrisia): Call<UserNutrisia>

    @POST("api/kelompok_1/login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/kelompok_1/select_profile.php")
    fun getProfile(@Body params: Map<String, String>): Call<SelectProfileResponse>

    @POST("api/kelompok_1/select_profile.php")
    fun getProgram(@Body params: Map<String, String>): Call<ViewProgramResponse>

    @POST("api/kelompok_1/insert_profile.php")
    fun insertProfile(@Body profile: InsertProfile): Call<InsertProfile>

    @Multipart
    @POST("api/kelompok_1/update_profile.php")
    fun updateProfile(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part foto: MultipartBody.Part?
    ): Call<ProfileResponse>

    @POST("api/kelompok_1/save_calories.php")
    suspend fun postScanCalorie(
        @Body request: ScanCalorieRequest
    ): Response<ApiResponse>
}


