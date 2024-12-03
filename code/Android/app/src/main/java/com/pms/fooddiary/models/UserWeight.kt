package com.pms.fooddiary.models

import java.util.Date

data class UserWeightRead(
    val id: Int? = null,
    val user_id: Int? = null,
    val weight: Float? = 0f,
    val recorded_at: Date? = null
)

data class UserWeightCreate(
    val weight: Float? = 0f,
)

data class UserWeightUpdate(
    val weight: Float? = 0f,
)
