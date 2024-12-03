package com.pms.fooddiary.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

data class MealCreate(
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var products: List<MealProductsCreate>? = null
)

data class MealUpdate(
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var products: List<MealProductsUpdate>? = null
)


@Parcelize
data class MealRead(
    val id: Int? = null,
    var name: String? = null,
    var weight: Double,
    var calories: Double,
    var proteins: Double,
    var fats: Double,
    var carbohydrates: Double,
    var user_id: Int? = null,
    var recorded_at: Date? = null,
    var products: List<ProductRead>? = null
) : Parcelable
