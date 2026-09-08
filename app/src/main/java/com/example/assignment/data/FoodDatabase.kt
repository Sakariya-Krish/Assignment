package com.example.assignment.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FoodItem::class, ShoppingItem::class, WasteRecord::class, FoodSavedRecord::class], 
    version = 5,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDatabase(database.foodDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(foodDao: FoodDao) {
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L
            
            val sampleItems = listOf(
                FoodItem(name = "Milk", category = "Dairy", quantity = "2", unit = "Litres", purchaseDate = now, expiryDate = now + 3 * day, storageLocation = "Refrigerator"),
                FoodItem(name = "Bread", category = "Bakery", quantity = "1", unit = "Pack", purchaseDate = now, expiryDate = now + 2 * day, storageLocation = "Kitchen"),
                FoodItem(name = "Apples", category = "Fruits", quantity = "6", unit = "Pieces", purchaseDate = now, expiryDate = now + 5 * day, storageLocation = "Refrigerator"),
                FoodItem(name = "Tomatoes", category = "Vegetables", quantity = "5", unit = "Pieces", purchaseDate = now, expiryDate = now - 1 * day, storageLocation = "Refrigerator"), // Expired
                FoodItem(name = "Cheese", category = "Dairy", quantity = "1", unit = "Pack", purchaseDate = now, expiryDate = now + 0 * day, storageLocation = "Refrigerator"), // Today
                FoodItem(name = "Chicken", category = "Meat", quantity = "500", unit = "Grams", purchaseDate = now, expiryDate = now + 1 * day, storageLocation = "Freezer"),
                FoodItem(name = "Rice", category = "Packaged", quantity = "5", unit = "Kg", purchaseDate = now, expiryDate = now + 30 * day, storageLocation = "Kitchen"),
                FoodItem(name = "Orange Juice", category = "Beverages", quantity = "1", unit = "Bottle", purchaseDate = now, expiryDate = now + 7 * day, storageLocation = "Refrigerator"),
                FoodItem(name = "Eggs", category = "Dairy", quantity = "12", unit = "Pieces", purchaseDate = now, expiryDate = now + 10 * day, storageLocation = "Refrigerator"),
                FoodItem(name = "Biscuits", category = "Packaged", quantity = "2", unit = "Packs", purchaseDate = now, expiryDate = now + 20 * day, storageLocation = "Kitchen")
            )
            
            sampleItems.forEach { foodDao.insert(it) }
        }
    }
}
