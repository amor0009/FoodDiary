package com.pms.fooddiary.API

import com.pms.fooddiary.models.UserWeightRead
import com.pms.fooddiary.models.UserWeightUpdate
import retrofit2.Call
import retrofit2.http.*

interface UserWeightAPIService {

    @PUT("/user_weight/me")
    fun updateUserWeight(
        @Body userWeightUpdate: UserWeightUpdate
    ): Call<UserWeightRead>

    @GET("/user_weight/me/{recordedAt}")
    fun getUserWeight(
        @Path("recordedAt") recordedAt: String
    ): Call<UserWeightRead>

    @GET("/user_weight/history/me")
    fun getUserWeightHistory(): Call<List<UserWeightRead>>
}
