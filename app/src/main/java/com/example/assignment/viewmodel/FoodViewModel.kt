package com.example.assignment.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.data.*
import com.example.assignment.util.DateUtils
import com.example.assignment.util.ExpiryStatus
import com.example.assignment.util.getExpiryStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository
    private val settingsManager = SettingsManager(application)
    private val gson = Gson()
    
    val allItems: StateFlow<List<FoodItem>>
    val allShoppingItems: StateFlow<List<ShoppingItem>>
    val allWasteRecords: StateFlow<List<WasteRecord>>
    val savedCount: StateFlow<Int>
    val frequentlyConsumedItems: StateFlow<List<FoodItem>>
    val allGoals: StateFlow<List<Goal>>
    val activityHistory: StateFlow<List<ActivityRecord>>
    val userProfile: StateFlow<UserProfile?>
    
    val notificationsEnabled = settingsManager.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val darkModeEnabled = settingsManager.darkModeEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        val foodDao = FoodDatabase.getDatabase(application).foodDao()
        repository = FoodRepository(foodDao)
        
        allItems = repository.allItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allShoppingItems = repository.allShoppingItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allWasteRecords = repository.allWasteRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        savedCount = repository.savedCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        frequentlyConsumedItems = repository.frequentlyConsumedItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        activityHistory = repository.allActivityRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
    }

    // User Profile Actions
    fun updateProfile(name: String, imageUri: String?, currency: String, unit: String) = viewModelScope.launch {
        try {
            repository.updateUserProfile(UserProfile(name = name, profileImageUri = imageUri, currency = currency, preferredUnit = unit))
        } catch (e: Exception) {
            Log.e("FoodViewModel", "Error updating profile", e)
        }
    }

    fun insert(item: FoodItem) = viewModelScope.launch { 
        repository.insert(item)
        repository.insertActivityRecord(ActivityRecord(foodName = item.name, action = "ADDED", details = "Added to inventory"))
    }
    fun update(item: FoodItem) = viewModelScope.launch { repository.update(item) }
    fun delete(item: FoodItem) = viewModelScope.launch { repository.delete(item) }
    
    fun markAsConsumed(item: FoodItem) = viewModelScope.launch { 
        repository.update(item.copy(status = FoodStatus.CONSUMED))
        repository.insertSavedRecord(FoodSavedRecord(foodName = item.name))
        repository.insertActivityRecord(ActivityRecord(foodName = item.name, action = "CONSUMED", details = "Marked as consumed"))
        updateGoalProgress("FOOD_SAVED", 1f)
    }
    
    fun markAsWasted(item: FoodItem, reason: String) = viewModelScope.launch { 
        repository.update(item.copy(status = FoodStatus.WASTED))
        repository.insertWasteRecord(WasteRecord(
            foodName = item.name,
            category = item.category,
            quantity = item.quantity,
            price = item.price,
            reason = reason
        ))
        repository.insertActivityRecord(ActivityRecord(foodName = item.name, action = "WASTED", details = "Reason: $reason"))
        updateGoalProgress("WASTE_TRACKED", 1f)
    }
    
    fun clearAllData() = viewModelScope.launch { repository.deleteAll() }

    // Shopping List Actions
    fun addShoppingItem(name: String, quantity: String, unit: String, category: String, notes: String) = viewModelScope.launch {
        repository.insertShoppingItem(ShoppingItem(
            name = name, 
            quantity = quantity, 
            unit = unit, 
            category = category, 
            notes = notes
        ))
        repository.insertActivityRecord(ActivityRecord(foodName = name, action = "ADDED_TO_SHOPPING", details = "Added to shopping list"))
    }

    fun updateShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.updateShoppingItem(item)
    }

    fun toggleShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        val newState = !item.isPurchased
        repository.updateShoppingItem(item.copy(
            isPurchased = newState,
            purchasedAt = if (newState) System.currentTimeMillis() else null
        ))
        if (newState) {
            repository.insertActivityRecord(ActivityRecord(foodName = item.name, action = "PURCHASED", details = "Marked as purchased"))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.deleteShoppingItem(item)
    }

    fun clearPurchasedShoppingItems() = viewModelScope.launch {
        repository.clearPurchasedShoppingItems()
    }

    // Goals
    fun addGoal(title: String, target: Float, type: String) = viewModelScope.launch {
        repository.insertGoal(Goal(title = title, targetValue = target, type = type))
    }
    
    private suspend fun updateGoalProgress(type: String, increment: Float) {
        val activeGoals = allGoals.value.filter { it.type == type && !it.isCompleted }
        activeGoals.forEach { goal ->
            val newValue = goal.currentValue + increment
            repository.updateGoal(goal.copy(
                currentValue = newValue,
                isCompleted = newValue >= goal.targetValue
            ))
        }
    }

    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch { settingsManager.setNotificationsEnabled(enabled) }
    fun setDarkMode(enabled: Boolean?) = viewModelScope.launch { settingsManager.setDarkModeEnabled(enabled) }

    val stats = combine(allItems, allWasteRecords, savedCount) { items, wasteRecords, saved ->
        val total = items.size
        val consumed = items.count { it.status == FoodStatus.CONSUMED }
        val expired = items.count { it.status == FoodStatus.FRESH && getExpiryStatus(it.expiryDate) == ExpiryStatus.EXPIRED }
        val totalWasteCount = wasteRecords.size
        
        val expiringToday = items.count { it.status == FoodStatus.FRESH && getExpiryStatus(it.expiryDate) == ExpiryStatus.EXPIRING_TODAY }
        val expiringThisWeek = items.count { 
            val status = getExpiryStatus(it.expiryDate)
            it.status == FoodStatus.FRESH && (status == ExpiryStatus.EXPIRING_SOON || status == ExpiryStatus.EXPIRING_TODAY)
        }

        val wastePercentage = if (consumed + totalWasteCount > 0) {
            (totalWasteCount.toFloat() / (consumed + totalWasteCount)) * 100
        } else 0f
        
        val wasteReduction = if (wastePercentage < 20) 20 - wastePercentage else 0f

        // Financial Stats
        val totalFoodCost = items.sumOf { it.price }
        val moneyWasted = wasteRecords.sumOf { it.price }

        // Food Saving Score calculation
        val deductions = (totalWasteCount * 5) + (expired * 2)
        val savingScore = (100 - deductions).coerceIn(0, 100)
        
        val mostWastedCategory = wasteRecords.groupBy { it.category }
            .maxByOrNull { it.value.size }?.key ?: "N/A"
            
        val mostWastedFood = wasteRecords.groupBy { it.foodName }
            .maxByOrNull { it.value.size }?.key ?: "N/A"

        val achievements = mutableListOf<Achievement>()
        if (total > 0) achievements.add(Achievement("First Food Added", "Added your first item", true))
        if (saved > 0) achievements.add(Achievement("First Food Saved", "Consumed food before expiry", true))
        if (saved >= 5) achievements.add(Achievement("5 Foods Saved", "Saved 5 items from waste", true))
        if (saved >= 10) achievements.add(Achievement("10 Foods Saved", "Waste reduction pro!", true))
        if (wastePercentage < 10 && total > 5) achievements.add(Achievement("Waste Reducer", "Kept waste under 10%", true))

        EnhancedStats2(
            total = total,
            fresh = items.count { it.status == FoodStatus.FRESH && getExpiryStatus(it.expiryDate) != ExpiryStatus.EXPIRED },
            consumed = consumed,
            wasted = totalWasteCount,
            expired = expired,
            expiringToday = expiringToday,
            expiringThisWeek = expiringThisWeek,
            wastePercentage = wastePercentage,
            wasteReduction = wasteReduction,
            savedCount = saved,
            mostWastedCategory = mostWastedCategory,
            mostWastedFood = mostWastedFood,
            savingScore = savingScore,
            totalFoodCost = totalFoodCost,
            moneyWasted = moneyWasted,
            achievements = achievements
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnhancedStats2())

    val recipeSuggestions = allItems.map { items ->
        val freshItems = items.filter { it.status == FoodStatus.FRESH }.map { it.name.lowercase() }
        suggestAdvancedRecipes(freshItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMissingIngredientsToShoppingList(recipe: Recipe) = viewModelScope.launch {
        recipe.missingIngredients.forEach { ingredient ->
            if (allShoppingItems.value.none { it.name.equals(ingredient, ignoreCase = true) && !it.isPurchased }) {
                repository.insertShoppingItem(ShoppingItem(name = ingredient, quantity = "1", category = "Other"))
            }
        }
    }

    val shoppingSuggestions = combine(allItems, allShoppingItems) { foodItems, shopItems ->
        val currentShopNames = shopItems.map { it.name.lowercase() }
        foodItems.filter { it.status == FoodStatus.CONSUMED }
            .groupBy { it.name }
            .filter { (name, _) -> !currentShopNames.contains(name.lowercase()) }
            .map { it.value.first() }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Food Insights
    val insights = stats.map { s ->
        val list = mutableListOf<String>()
        if (s.expiringToday > 0) list.add("You have ${s.expiringToday} items expiring today!")
        if (s.expired > 0) list.add("You have ${s.expired} expired items. Clear them to see better stats.")
        if (s.wasted > 0) list.add("${s.mostWastedCategory} is your most wasted category.")
        if (s.savedCount > 0) list.add("You've saved ${s.savedCount} items! Great impact.")
        if (s.wastePercentage > 20) list.add("Your waste is a bit high. Check the 'Use First' section.")
        if (list.isEmpty()) list.add("Your food inventory looks healthy!")
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Loading insights..."))

    // Backup & Restore
    fun exportData(context: Context): Uri? {
        val backupData = BackupData(
            foodItems = allItems.value,
            shoppingItems = allShoppingItems.value,
            wasteRecords = allWasteRecords.value,
            goals = allGoals.value
        )
        val json = gson.toJson(backupData)
        return try {
            val file = File(context.cacheDir, "foodtrack_backup.json")
            file.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) { 
            Log.e("FoodViewModel", "Error exporting data", e)
            null 
        }
    }

    fun importData(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) {
                val backupData = gson.fromJson(json, BackupData::class.java)
                backupData.foodItems.forEach { repository.insert(it) }
                backupData.shoppingItems.forEach { repository.insertShoppingItem(it) }
                backupData.wasteRecords.forEach { repository.insertWasteRecord(it) }
                backupData.goals.forEach { repository.insertGoal(it) }
                repository.insertActivityRecord(ActivityRecord(foodName = "Backup", action = "RESTORED", details = "Data restored from backup"))
            }
        } catch (e: Exception) { 
            Log.e("FoodViewModel", "Error importing data", e)
        }
    }

    // Assistant Logic
    private val _chatMessages = MutableStateFlow(listOf(Message("Assistant", "Hi! I'm your Food Assistant. How can I help you today?")))
    val chatMessages = _chatMessages.asStateFlow()

    fun sendMessage(text: String) {
        val userMessage = Message("User", text)
        _chatMessages.update { it + userMessage }
        
        viewModelScope.launch {
            val response = generateAssistantResponse(text)
            _chatMessages.update { it + Message("Assistant", response) }
        }
    }

    private suspend fun generateAssistantResponse(input: String): String {
        val query = input.lowercase()
        val items = allItems.value.filter { it.status == FoodStatus.FRESH }
        
        return when {
            query.contains("eat first") || query.contains("expire") || query.contains("what should i eat") -> {
                val soon = items.sortedBy { it.expiryDate }.take(3)
                if (soon.isEmpty()) "You don't have any fresh food items."
                else "You should use these soon: " + soon.joinToString(", ") { it.name }
            }
            query.contains("cook") || query.contains("recipe") || query.contains("what can i cook") -> {
                val suggestions = recipeSuggestions.value
                if (suggestions.isEmpty()) "I couldn't find recipes for your current ingredients. Try adding more variety."
                else {
                    val recipe = suggestions.first()
                    "You have almost everything for a ${recipe.name}. You just need ${recipe.missingIngredients.joinToString(", ")}."
                }
            }
            query.contains("waste") || query.contains("wasting") -> {
                val s = stats.value
                "Your most wasted category is ${s.mostWastedCategory} and most wasted item is ${s.mostWastedFood}."
            }
            query.contains("saved") || query.contains("impact") -> {
                val s = stats.value
                "You have saved ${s.savedCount} food items so far! Your food saving score is ${s.savingScore}/100."
            }
            else -> "I can help you with what to eat first, recipe suggestions based on your food, or waste analysis. What would you like to know?"
        }
    }
}

data class BackupData(
    val foodItems: List<FoodItem>,
    val shoppingItems: List<ShoppingItem>,
    val wasteRecords: List<WasteRecord>,
    val goals: List<Goal>
)

data class Message(val sender: String, val text: String)

data class Achievement(val title: String, val description: String, val isUnlocked: Boolean)

data class EnhancedStats2(
    val total: Int = 0,
    val fresh: Int = 0,
    val consumed: Int = 0,
    val wasted: Int = 0,
    val expired: Int = 0,
    val expiringToday: Int = 0,
    val expiringThisWeek: Int = 0,
    val wastePercentage: Float = 0f,
    val wasteReduction: Float = 0f,
    val savedCount: Int = 0,
    val mostWastedCategory: String = "N/A",
    val mostWastedFood: String = "N/A",
    val savingScore: Int = 0,
    val totalFoodCost: Double = 0.0,
    val moneyWasted: Double = 0.0,
    val achievements: List<Achievement> = emptyList()
)

data class Recipe(
    val name: String, 
    val ingredients: List<String>, 
    val steps: String,
    val cookingTime: String = "15 mins",
    val availableIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList()
)

fun suggestAdvancedRecipes(availableItems: List<String>): List<Recipe> {
    val recipes = listOf(
        Recipe("Tomato Sandwich", listOf("tomato", "bread", "onion"), "1. Slice tomatoes and onions. 2. Place on bread. 3. Toast if desired.", "5 mins"),
        Recipe("Fruit Salad", listOf("apple", "banana", "orange", "grapes"), "1. Chop all fruits. 2. Mix in a bowl. 3. Serve chilled.", "10 mins"),
        Recipe("Omelette", listOf("egg", "onion", "tomato", "pepper"), "1. Whisk eggs. 2. Sauté veggies. 3. Pour eggs and cook until firm.", "10 mins"),
        Recipe("Pasta with Veggies", listOf("pasta", "tomato", "onion", "garlic"), "1. Boil pasta. 2. Make sauce with veggies. 3. Mix and enjoy.", "20 mins")
    )
    
    return recipes.map { recipe ->
        val available = recipe.ingredients.filter { ing -> availableItems.any { it.contains(ing) } }
        val missing = recipe.ingredients.filter { ing -> availableItems.none { it.contains(ing) } }
        recipe.copy(availableIngredients = available, missingIngredients = missing)
    }.filter { it.availableIngredients.isNotEmpty() }
     .sortedBy { it.missingIngredients.size }
}
