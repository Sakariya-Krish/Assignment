package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.data.FoodItem
import com.example.assignment.data.FoodStatus
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import java.util.*

@Composable
fun HomeScreen(
    viewModel: FoodViewModel, 
    onNavigateToDetails: (Int) -> Unit, 
    onNavigateToShopping: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val shoppingItems by viewModel.allShoppingItems.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val recipes by viewModel.recipeSuggestions.collectAsState()
    val insights by viewModel.insights.collectAsState()

    var selectedRecipe by remember { mutableStateOf<com.example.assignment.viewmodel.Recipe?>(null) }

    val freshItems = items.filter { it.status == FoodStatus.FRESH }
    val expiringToday = freshItems.filter { isSameDay(it.expiryDate, System.currentTimeMillis()) }
    val useFirst = freshItems.sortedBy { it.expiryDate }.take(5)
    
    val toBuyCount = shoppingItems.count { !it.isPurchased }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "FoodTrack v3.0",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = FoodGreen
                )
            }

            // Insights Section
            item {
                AnimatedContent(targetState = insights, label = "InsightsAnimation") { currentInsights ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Food Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            currentInsights.take(3).forEach { insight ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(insight, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Add Food", Icons.Default.Add, Modifier.weight(1f), onNavigateToAdd)
                    QuickActionCard("Scan Barcode", Icons.Default.QrCodeScanner, Modifier.weight(1f), onNavigateToScanner)
                    QuickActionCard("Grocery List", Icons.Default.ShoppingCart, Modifier.weight(1f), onNavigateToShopping)
                }
            }

            // Impact Score
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FoodGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Food Saving Score", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Waste Reduction: ${String.format(Locale.getDefault(), "%.0f", stats.wasteReduction)}%", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${stats.savingScore}", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = stats.savingScore / 100f,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Statistics Summary
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Expiring Today", stats.expiringToday.toString(), Modifier.weight(1f), if(stats.expiringToday > 0) Color.Red else Color.Gray, Icons.Default.Warning)
                    StatCard("Saved Items", stats.savedCount.toString(), Modifier.weight(1f), FoodGreen, Icons.Default.CheckCircle)
                }
            }

            // Shopping Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToShopping),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shopping List", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(if (toBuyCount > 0) "$toBuyCount items to buy" else "Everything purchased", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }
            }

            if (expiringToday.isNotEmpty()) {
                item { SectionHeader("Expiring Today", Color.Red) }
                items(expiringToday) { item ->
                    FoodSummaryItem(item, onClick = { onNavigateToDetails(item.id) })
                }
            }

            if (useFirst.isNotEmpty()) {
                item { SectionHeader("Use First", FoodGreen) }
                items(useFirst) { item ->
                    FoodSummaryItem(item, onClick = { onNavigateToDetails(item.id) })
                }
            }

            if (recipes.isNotEmpty()) {
                item { SectionHeader("What Can I Cook?", FoodGreen) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                        items(recipes) { recipe -> 
                            RecipeCard(recipe, onClick = { selectedRecipe = recipe }) 
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            onAddMissing = {
                viewModel.addMissingIngredientsToShoppingList(selectedRecipe!!)
                selectedRecipe = null
            }
        )
    }
}

@Composable
fun RecipeDetailsDialog(recipe: com.example.assignment.viewmodel.Recipe, onDismiss: () -> Unit, onAddMissing: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cooking Time: ${recipe.cookingTime}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                Text("Ingredients:", fontWeight = FontWeight.Bold)
                recipe.ingredients.forEach { ing ->
                    val isAvailable = recipe.availableIngredients.contains(ing)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isAvailable) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isAvailable) FoodGreen else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(ing, color = if (isAvailable) Color.Unspecified else Color.Gray)
                    }
                }

                Text("Steps:", fontWeight = FontWeight.Bold)
                Text(recipe.steps)
                
                if (recipe.missingIngredients.isNotEmpty()) {
                    Button(
                        onClick = onAddMissing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text("Add Missing Items to Shopping List")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = FoodGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun RecipeCard(recipe: com.example.assignment.viewmodel.Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(200.dp).height(140.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(recipe.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("⏱ ${recipe.cookingTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            val matchText = "${recipe.availableIngredients.size}/${recipe.ingredients.size} match"
            Text(matchText, style = MaterialTheme.typography.labelSmall, color = FoodGreen, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.weight(1f))
            Text("View Recipe", style = MaterialTheme.typography.labelSmall, color = FoodGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun FoodSummaryItem(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            val daysLeft = ((item.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            
            val statusText = when {
                item.status == FoodStatus.CONSUMED -> "Consumed"
                item.status == FoodStatus.WASTED -> "Wasted"
                daysLeft < 0 -> "Expired"
                daysLeft == 0 -> "Today"
                else -> "$daysLeft d"
            }
            
            val color = when {
                item.status == FoodStatus.CONSUMED -> Color.Gray
                item.status == FoodStatus.WASTED -> Color.Gray
                daysLeft < 0 -> Color.Red
                daysLeft < 3 -> Color(0xFFFF9800)
                else -> FoodGreen
            }

            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = statusText,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
