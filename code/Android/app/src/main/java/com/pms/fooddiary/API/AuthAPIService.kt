package com.pms.fooddiary.API

import com.pms.fooddiary.models.AuthResponse
import com.pms.fooddiary.models.UserRegistration
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.POST

interface AuthAPIService {

    @POST("/auth/registration")
    fun registerUser(
        @Body user: UserRegistration
    ): Call<AuthResponse>

    @POST("/auth/login")
    fun loginUser(
        @Query("email_login") email_login: String,
        @Query("password") password: String
    ): Call<AuthResponse>
}
