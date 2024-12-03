package com.pms.fooddiary.API

import LocalDateAdapter
import android.content.Context
import com.google.gson.GsonBuilder
import com.pms.fooddiary.security.TokenInterceptor
import com.pms.fooddiary.security.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate


object APIClient {
    // for emulator device = http://10.0.2.2:8000
    // for real device with the same wifi connection = http://192.168.1.10:8000
    // for devices connected to my phone traffic= http://172.20.10.2:8000
    private const val BASE_URL = "http://10.0.2.2:8000"

    fun createRetrofit(context: Context): Retrofit {
        val tokenManager = TokenManager(context)
        val tockenInterceptor = TokenInterceptor(tokenManager)

        val client = OkHttpClient.Builder()
            .addInterceptor(tockenInterceptor)
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())  // Для LocalDate
            .registerTypeAdapter(java.sql.Date::class.java, LocalDateAdapter())  // Для sql.Date
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
