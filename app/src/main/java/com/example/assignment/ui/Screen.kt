package com.example.assignment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object FoodList : Screen("food_list", "Food", Icons.Default.List)
    object AddFood : Screen("add_food?foodId={foodId}&scannedBarcode={scannedBarcode}&preName={preName}&preCategory={preCategory}&preQuantity={preQuantity}", "Add", Icons.Default.Add)
    object ShoppingList : Screen("shopping_list", "Shopping", Icons.Default.ShoppingCart)
    object More : Screen("more", "More", Icons.Default.Menu)
    
    // Sub-screens under "More"
    object Statistics : Screen("statistics", "Stats", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object FoodDetails : Screen("food_details/{foodId}", "Details", Icons.Default.Info)
    object BarcodeScanner : Screen("barcode_scanner", "Scanner", Icons.Default.QrCodeScanner)
    object FoodAssistant : Screen("food_assistant", "Assistant", Icons.Default.Assistant)
    object UseFirst : Screen("use_first", "Use First", Icons.Default.PriorityHigh)
    object ExpiryCalendar : Screen("expiry_calendar", "Calendar", Icons.Default.CalendarMonth)
    object ActivityHistory : Screen("activity_history", "Timeline", Icons.Default.History)
    object Achievements : Screen("achievements", "Achievements", Icons.Default.Star)
}
