package com.example.assignment.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.assignment.ui.screens.*
import com.example.assignment.viewmodel.FoodViewModel

@Composable
fun FoodTrackApp() {
    val navController = rememberNavController()
    val viewModel: FoodViewModel = viewModel()
    val navItems = listOf(Screen.Home, Screen.FoodList, Screen.AddFood, Screen.ShoppingList, Screen.More)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route.split("?")[0]) == true } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    viewModel = viewModel, 
                    onNavigateToDetails = { id ->
                        navController.navigate(Screen.FoodDetails.route.replace("{foodId}", id.toString()))
                    },
                    onNavigateToShopping = {
                        navController.navigate(Screen.ShoppingList.route)
                    },
                    onNavigateToAdd = {
                        navController.navigate("add_food?foodId=-1")
                    },
                    onNavigateToScanner = {
                        navController.navigate(Screen.BarcodeScanner.route)
                    }
                ) 
            }
            composable(Screen.FoodList.route) { 
                FoodListScreen(viewModel, onNavigateToDetails = { id ->
                    navController.navigate(Screen.FoodDetails.route.replace("{foodId}", id.toString()))
                }) 
            }
            composable(Screen.ShoppingList.route) { 
                ShoppingListScreen(
                    viewModel = viewModel,
                    onNavigateToAddFood = { name, category, quantity ->
                        navController.navigate("add_food?foodId=-1&preName=$name&preCategory=$category&preQuantity=$quantity")
                    }
                ) 
            }
            composable(Screen.More.route) {
                MoreScreen(onNavigate = { route -> navController.navigate(route) })
            }
            
            // Sub-screens
            composable(Screen.Statistics.route) { StatsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable(Screen.FoodAssistant.route) { FoodAssistantScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable(Screen.UseFirst.route) { 
                UseFirstScreen(
                    viewModel = viewModel, 
                    onNavigateToDetails = { id -> navController.navigate(Screen.FoodDetails.route.replace("{foodId}", id.toString())) },
                    onBack = { navController.popBackStack() }
                ) 
            }
            composable(Screen.ExpiryCalendar.route) {
                ExpiryCalendarScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> navController.navigate(Screen.FoodDetails.route.replace("{foodId}", id.toString())) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Achievements.route) { AchievementsScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable(Screen.ActivityHistory.route) { ActivityHistoryScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel, onBack = { navController.popBackStack() }) }

            composable(
                route = "add_food?foodId={foodId}&scannedBarcode={scannedBarcode}&preName={preName}&preCategory={preCategory}&preQuantity={preQuantity}",
                arguments = listOf(
                    navArgument("foodId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("scannedBarcode") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("preName") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("preCategory") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("preQuantity") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getInt("foodId") ?: -1
                val scannedBarcode = backStackEntry.arguments?.getString("scannedBarcode")
                val preName = backStackEntry.arguments?.getString("preName")
                val preCategory = backStackEntry.arguments?.getString("preCategory")
                val preQuantity = backStackEntry.arguments?.getString("preQuantity")
                
                AddFoodScreen(
                    foodId = foodId, 
                    viewModel = viewModel, 
                    onSaved = {
                        navController.navigate(Screen.FoodList.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onNavigateToScanner = {
                        navController.navigate(Screen.BarcodeScanner.route)
                    },
                    scannedBarcode = scannedBarcode,
                    preName = preName,
                    preCategory = preCategory,
                    preQuantity = preQuantity
                ) 
            }
            
            composable(
                route = Screen.FoodDetails.route,
                arguments = listOf(navArgument("foodId") { type = NavType.IntType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getInt("foodId") ?: 0
                FoodDetailsScreen(
                    foodId, 
                    viewModel, 
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate("add_food?foodId=$id")
                    },
                    onNavigateToShopping = {
                        navController.navigate(Screen.ShoppingList.route)
                    }
                )
            }
            composable(Screen.BarcodeScanner.route) {
                BarcodeScannerScreen(
                    onBarcodeScanned = { barcode ->
                        navController.navigate("add_food?scannedBarcode=$barcode") {
                            popUpTo(Screen.BarcodeScanner.route) { inclusive = true }
                        }
                    },
                    onSearchManual = { name, _ ->
                        navController.navigate("add_food?preName=$name") {
                            popUpTo(Screen.BarcodeScanner.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
