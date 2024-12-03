package com.pms.fooddiary.models

data class MealProductsRead(
    val product_weight: Double? = null,
    val meal_id: Int? = null,
    val product_id: Int? = null
)

data class MealProductsUpdate(
    val product_weight: Double? = null,
    val product_id: Int? = null
)

data class MealProductsCreate(
    val product_weight: Double? = null,
    val product_id: Int? = null
)