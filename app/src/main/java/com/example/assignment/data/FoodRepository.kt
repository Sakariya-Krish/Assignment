package com.example.assignment.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {
    val allItems: Flow<List<FoodItem>> = foodDao.getAllItems()
    val allShoppingItems: Flow<List<ShoppingItem>> = foodDao.getAllShoppingItems()
    val allWasteRecords: Flow<List<WasteRecord>> = foodDao.getAllWasteRecords()
    val savedCount: Flow<Int> = foodDao.getSavedCount()

    suspend fun getItemById(id: Int): FoodItem? = foodDao.getItemById(id)
    suspend fun insert(item: FoodItem) = foodDao.insert(item)
    suspend fun update(item: FoodItem) = foodDao.update(item)
    suspend fun delete(item: FoodItem) = foodDao.delete(item)
    suspend fun deleteAll() = foodDao.deleteAll()

    suspend fun insertShoppingItem(item: ShoppingItem) = foodDao.insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = foodDao.updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = foodDao.deleteShoppingItem(item)
    suspend fun clearPurchasedShoppingItems() = foodDao.clearPurchasedItems()

    val frequentlyConsumedItems: Flow<List<FoodItem>> = foodDao.getFrequentlyConsumedItems()

    suspend fun insertWasteRecord(record: WasteRecord) = foodDao.insertWasteRecord(record)
    suspend fun insertSavedRecord(record: FoodSavedRecord) = foodDao.insertSavedRecord(record)

    val allActivityRecords: Flow<List<ActivityRecord>> = foodDao.getAllActivityRecords()
    suspend fun insertActivityRecord(record: ActivityRecord) = foodDao.insertActivityRecord(record)

    val allGoals: Flow<List<Goal>> = foodDao.getAllGoals()
    suspend fun insertGoal(goal: Goal) = foodDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = foodDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = foodDao.deleteGoal(goal)

    val userProfile: Flow<UserProfile?> = foodDao.getUserProfile()
    suspend fun updateUserProfile(profile: UserProfile) = foodDao.updateUserProfile(profile)
}
