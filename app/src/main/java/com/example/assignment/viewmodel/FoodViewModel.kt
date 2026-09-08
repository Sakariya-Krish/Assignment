package com.example.assignment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository
    private val settingsManager = SettingsManager(application)
    
    val allItems: StateFlow<List<FoodItem>>
    val allShoppingItems: StateFlow<List<ShoppingItem>>
    val allWasteRecords: StateFlow<List<WasteRecord>>
    val savedCount: StateFlow<Int>
    val frequentlyConsumedItems: StateFlow<List<FoodItem>>
    
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
    }

    fun insert(item: FoodItem) = viewModelScope.launch { repository.insert(item) }
    fun update(item: FoodItem) = viewModelScope.launch { repository.update(item) }
    fun delete(item: FoodItem) = viewModelScope.launch { repository.delete(item) }
    
    fun markAsConsumed(item: FoodItem) = viewModelScope.launch { 
        repository.update(item.copy(status = FoodStatus.CONSUMED))
        repository.insertSavedRecord(FoodSavedRecord(foodName = item.name))
    }
    
    fun markAsWasted(item: FoodItem, reason: String) = viewModelScope.launch { 
        repository.update(item.copy(status = FoodStatus.WASTED))
        repository.insertWasteRecord(WasteRecord(
            foodName = item.name,
            category = item.category,
            quantity = item.quantity,
            reason = reason
        ))
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
    }

    fun updateShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.updateShoppingItem(item)
    }

    fun toggleShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.updateShoppingItem(item.copy(
            isPurchased = !item.isPurchased,
            purchasedAt = if (!item.isPurchased) System.currentTimeMillis() else null
        ))
    }

    fun deleteShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.deleteShoppingItem(item)
    }

    fun clearPurchasedShoppingItems() = viewModelScope.launch {
        repository.clearPurchasedShoppingItems()
    }

    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch { settingsManager.setNotificationsEnabled(enabled) }
    fun setDarkMode(enabled: Boolean?) = viewModelScope.launch { settingsManager.setDarkModeEnabled(enabled) }

    val stats = combine(allItems, allWasteRecords, savedCount) { items, wasteRecords, saved ->
        val total = items.size
        val consumed = items.count { it.status == FoodStatus.CONSUMED }
        val expired = items.count { it.status == FoodStatus.FRESH && it.expiryDate < System.currentTimeMillis() }
        
        val totalWaste = wasteRecords.size
        val wastePercentage = if (consumed + totalWaste > 0) {
            (totalWaste.toFloat() / (consumed + totalWaste)) * 100
        } else 0f
        
        val deductions = (totalWaste * 5) + (expired * 2)
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
            fresh = items.count { it.status == FoodStatus.FRESH && it.expiryDate >= System.currentTimeMillis() },
            consumed = consumed,
            wasted = totalWaste,
            expired = expired,
            wastePercentage = wastePercentage,
            savedCount = saved,
            mostWastedCategory = mostWastedCategory,
            mostWastedFood = mostWastedFood,
            savingScore = savingScore,
            achievements = achievements
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnhancedStats2())

    val recipeSuggestions = allItems.map { items ->
        val freshItems = items.filter { it.status == FoodStatus.FRESH }.map { it.name.lowercase() }
        suggestAdvancedRecipes(freshItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        if (s.expired > 0) list.add("You have ${s.expired} expired items. Clear them to see better stats.")
        if (s.wasted > 0) list.add("${s.mostWastedCategory} is your most wasted category.")
        if (s.savedCount > 0) list.add("You've saved ${s.savedCount} items! Great impact.")
        if (s.wastePercentage > 20) list.add("Your waste is a bit high. Check the 'Use First' section.")
        if (list.isEmpty()) list.add("Your food inventory looks healthy!")
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Loading insights..."))

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
            query.contains("eat first") || query.contains("expire") -> {
                val soon = items.sortedBy { it.expiryDate }.take(3)
                if (soon.isEmpty()) "You don't have any fresh food items."
                else "You should use these soon: " + soon.joinToString(", ") { it.name }
            }
            query.contains("cook") || query.contains("recipe") -> {
                val suggestions = recipeSuggestions.value
                if (suggestions.isEmpty()) "I couldn't find recipes for your current ingredients. Try adding more variety."
                else "You can make: " + suggestions.take(2).joinToString(" or ") { it.name }
            }
            query.contains("waste") -> {
                val s = stats.value
                "Your most wasted category is ${s.mostWastedCategory}."
            }
            else -> "I can help you with what to eat first, recipe suggestions based on your food, or waste analysis. What would you like to know?"
        }
    }
}

data class Message(val sender: String, val text: String)

data class Achievement(val title: String, val description: String, val isUnlocked: Boolean)

data class EnhancedStats2(
    val total: Int = 0,
    val fresh: Int = 0,
    val consumed: Int = 0,
    val wasted: Int = 0,
    val expired: Int = 0,
    val wastePercentage: Float = 0f,
    val savedCount: Int = 0,
    val mostWastedCategory: String = "N/A",
    val mostWastedFood: String = "N/A",
    val savingScore: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

data class Recipe(
    val name: String, 
    val ingredients: List<String>, 
    val steps: String,
    val cookingTime: String = "15 mins"
)

fun suggestAdvancedRecipes(availableItems: List<String>): List<Recipe> {
    val recipes = listOf(
        Recipe("Tomato Sandwich", listOf("tomato", "bread", "onion"), "1. Slice tomatoes and onions. 2. Place on bread. 3. Toast if desired.", "5 mins"),
        Recipe("Fruit Salad", listOf("apple", "banana", "orange", "grapes"), "1. Chop all fruits. 2. Mix in a bowl. 3. Serve chilled.", "10 mins"),
        Recipe("Omelette", listOf("egg", "onion", "tomato", "pepper"), "1. Whisk eggs. 2. Sauté veggies. 3. Pour eggs and cook until firm.", "10 mins"),
        Recipe("Pasta with Veggies", listOf("pasta", "tomato", "onion", "garlic"), "1. Boil pasta. 2. Make sauce with veggies. 3. Mix and enjoy.", "20 mins")
    )
    
    return recipes.filter { recipe ->
        recipe.ingredients.any { ingredient -> availableItems.any { it.contains(ingredient) } }
    }
}
