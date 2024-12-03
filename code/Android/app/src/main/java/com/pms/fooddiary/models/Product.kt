package com.pms.fooddiary.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ProductResponse(val status: String, val user: UserRead)

data class ProductUpdate(
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var description: String? = null,
    var picture_path: String? = null
)

@Parcelize
data class ProductRead(
    val id: Int? = 0,
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var description: String? = null,
    var picture_path: String? = null
) : Parcelable

data class ProductCreate(
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var description: String? = null

)

data class ProductAdd(
    var name: String? = null,
    var weight: Float? = 0f
)
