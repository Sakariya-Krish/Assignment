package com.example.assignment.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFoodStatus(status: FoodStatus): String {
        return status.name
    }

    @TypeConverter
    fun toFoodStatus(status: String): FoodStatus {
        return try {
            FoodStatus.valueOf(status)
        } catch (e: Exception) {
            FoodStatus.FRESH
        }
    }
}
