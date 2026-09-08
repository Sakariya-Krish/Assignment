package com.example.assignment.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // Food Items
    @Query("SELECT * FROM food_items ORDER BY expiryDate ASC")
    fun getAllItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getItemById(id: Int): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodItem)

    @Update
    suspend fun update(item: FoodItem)

    @Delete
    suspend fun delete(item: FoodItem)

    @Query("DELETE FROM food_items")
    suspend fun deleteAll()

    // Shopping List
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, createdAt DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun clearPurchasedItems()

    @Query("SELECT * FROM food_items WHERE status = 'CONSUMED' GROUP BY name HAVING COUNT(*) >= 1")
    fun getFrequentlyConsumedItems(): Flow<List<FoodItem>>

    // Waste Records
    @Query("SELECT * FROM waste_records ORDER BY wasteDate DESC")
    fun getAllWasteRecords(): Flow<List<WasteRecord>>

    @Insert
    suspend fun insertWasteRecord(record: WasteRecord)

    // Food Saved Records
    @Query("SELECT COUNT(*) FROM food_saved_records")
    fun getSavedCount(): Flow<Int>

    @Insert
    suspend fun insertSavedRecord(record: FoodSavedRecord)
}
