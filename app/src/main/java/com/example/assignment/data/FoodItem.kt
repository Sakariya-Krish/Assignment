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
    val storageLocation: String = "Refrigerator",
    val createdAt: Long = System.currentTimeMillis()
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
    val wasteDate: Long = System.currentTimeMillis(),
    val reason: String = "Expired"
)

@Entity(tableName = "food_saved_records")
data class FoodSavedRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val savedDate: Long = System.currentTimeMillis()
)
