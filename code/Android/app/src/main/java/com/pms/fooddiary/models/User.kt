package com.pms.fooddiary.models

data class UserRegistration(val login: String,val email: String, val password: String)

data class AuthResponse(val access_token: String, val token_type: String)

data class UserResponse(val status: String, val user: UserRead)

data class UserUpdate(
    val login: String? = null,
    val email: String? = null,
    val password: String? = null,
    var lastname: String? = null,
    var firstname: String? = null,
    var age: Int? = 0,
    var height: Int? = 0,
    var weight: Float? = 0f,
    var gender: String? = null,
    var aim: String? = null,
    var recommended_calories: Float? = 0f,
    var profileImagePath: String? = null
)

data class UserRead(
    val id: Int,
    val login: String,
    val email: String,
    var lastname: String? = null,
    var firstname: String? = null,
    var age: Int? = 0,
    var height: Int? = 0,
    var weight: Float? = 0f,
    var gender: String? = null,
    var aim: String? = null,
    var recommended_calories: Float? = 0f,
    var profileImagePath: String? = null
)
