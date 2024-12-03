package com.pms.fooddiary.API

import com.pms.fooddiary.models.*
import retrofit2.Call
import retrofit2.http.*

interface MealAPIService {

    @POST("/meal/add")
    fun addMeal(
        @Body meal: MealCreate
    ): Call<MealRead>

    @GET("/meal/all_meals")
    fun getUserMeals(): Call<List<MealRead>>

    @GET("/meal/meals_products/{meal_id}")
    fun getMealProducts(
        @Path("meal_id") mealId: Int
    ): Call<List<ProductRead>>

    @GET("/meal/user_meals_with_products/info/{target_date}")
    fun getUserMealsWithProductsByDate(
        @Path("target_date") targetDate: String
    ): Call<List<MealRead>>

    @GET("/meal/id/{meal_id}")
    fun getMealById(
        @Path("meal_id") mealId: Int
    ): Call<MealRead>

    @GET("/meal/date/{target_date}")
    fun getMealsByDate(
        @Path("target_date") targetDate: String
    ): Call<List<MealRead>>

    @GET("/meal/history")
    fun getMealHistory(): Call<List<MealRead>>

    @PUT("/meal/{meal_id}")
    fun updateMeal(
        @Path("meal_id") mealId: Int,
        @Body mealUpdate: MealUpdate
    ): Call<MealRead>

    @DELETE("/meal/{meal_id}")
    fun deleteMeal(
        @Path("meal_id") mealId: Int
    ): Call<Void>
}
