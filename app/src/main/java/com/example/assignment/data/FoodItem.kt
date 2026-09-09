package com.example.assignment.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: String,
    val unit: String = "Pieces",
    val purchaseDate: Long,
    val expiryDate: Long,
    val notes: String = "",
    val status: FoodStatus = FoodStatus.FRESH,
    val imageUri: String? = null,
    val barcode: String? = null,
    val brand: String = "",
    val price: Double = 0.0,
    val storageLocation: String = "Refrigerator",
    val createdAt: Long = System.currentTimeMillis(),
    val lastNotificationMilestone: Int = -2 // -2: None, 3: 3 days, 1: 1 day, 0: Today, -1: Expired
)

enum class FoodStatus {
    FRESH, CONSUMED, WASTED, EXPIRED
}

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: String,
    val unit: String = "Pieces",
    val category: String = "Other",
    val notes: String = "",
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = null
)

@Entity(tableName = "waste_records")
data class WasteRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val category: String,
    val quantity: String,
    val price: Double = 0.0,
    val wasteDate: Long = System.currentTimeMillis(),
    val reason: String = "Expired"
)

@Entity(tableName = "food_saved_records")
data class FoodSavedRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val savedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_history")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val action: String, // ADDED, CONSUMED, WASTED, EXPIRED, PURCHASED
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val targetValue: Float,
    val currentValue: Float = 0f,
    val type: String, // WASTE_REDUCTION, FOOD_SAVED, etc.
    val deadline: Long? = null,
    val isCompleted: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "User",
    val profileImageUri: String? = null,
    val currency: String = "$",
    val preferredUnit: String = "Pieces"
)
