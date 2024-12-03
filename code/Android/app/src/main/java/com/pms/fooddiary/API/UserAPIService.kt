package com.pms.fooddiary.API

import com.pms.fooddiary.models.UserResponse
import com.pms.fooddiary.models.UserUpdate
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path


interface UserAPIService {

    @GET("/user/{login_email}")
    fun getUser(
        @Path("login_email") loginEmail: String
    ): Call<UserResponse>

    @GET("/user/me")
    fun getCurrentUser(): Call<UserResponse>

    @PUT("/user/me")
    fun updateUser(
        @Body user: UserUpdate
    ): Call<UserResponse>

    @DELETE("/user/me")
    fun deleteUser(): Call<UserResponse>
}
