package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val freshItems = items.filter { it.status == FoodStatus.FRESH }
    val expiringToday = freshItems.filter { isSameDay(it.expiryDate, System.currentTimeMillis()) }
    val useFirst = freshItems.sortedBy { it.expiryDate }.take(5)
    
    val toBuyCount = shoppingItems.count { !it.isPurchased }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("FoodTrack v3.0", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FoodGreen)
        }

        // Insights Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Food Insights", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    insights.take(2).forEach { insight ->
                        Text("• $insight", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Quick Actions
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionCard("Add", Icons.Default.Add, Modifier.weight(1f), onNavigateToAdd)
                QuickActionCard("Scan", Icons.Default.QrCodeScanner, Modifier.weight(1f), onNavigateToScanner)
                QuickActionCard("Shop", Icons.Default.ShoppingCart, Modifier.weight(1f), onNavigateToShopping)
            }
        }

        // Shopping List Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToShopping),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shopping List", fontWeight = FontWeight.Bold)
                        Text(if (toBuyCount > 0) "$toBuyCount items to buy" else "Everything purchased", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // Saving Score
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FoodGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Food Saving Score", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${stats.savingScore}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                        Text("/100", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = when {
                                stats.savingScore >= 90 -> "Excellent!"
                                stats.savingScore >= 70 -> "Good job!"
                                else -> "Keep improving!"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            item { SectionHeader("Cook Before Expiry", FoodGreen) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recipes) { recipe -> RecipeCard(recipe) }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = FoodGreen)
            Text(label, style = MaterialTheme.typography.labelSmall)
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
fun RecipeCard(recipe: com.example.assignment.viewmodel.Recipe) {
    Card(
        modifier = Modifier.width(200.dp).height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(recipe.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("⏱ ${recipe.cookingTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(recipe.ingredients.joinToString(", "), style = MaterialTheme.typography.bodySmall, maxLines = 2)
            Spacer(modifier = Modifier.weight(1f))
            Text("Cook Now", style = MaterialTheme.typography.labelSmall, color = FoodGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FoodSummaryItem(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
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
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp
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
