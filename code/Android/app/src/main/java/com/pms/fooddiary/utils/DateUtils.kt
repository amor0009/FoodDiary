package com.pms.fooddiary.utils

object DateUtils {
    init {
        System.loadLibrary("date_format")
    }

    external fun formatDateForAPI(date: String): String
}
