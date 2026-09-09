package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import java.util.Locale

@Composable
fun StatsScreen(viewModel: FoodViewModel) {
    val stats by viewModel.stats.collectAsState()
    val wasteRecords by viewModel.allWasteRecords.collectAsState()

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Impact Analysis", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = FoodGreen
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    colors = CardDefaults.cardColors(containerColor = FoodGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Food Saving Score", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("${stats.savingScore}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = Color.White)
                        Text(
                            text = if(stats.savingScore > 80) "Excellent Progress!" else "Keep Tracking!", 
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Text("Financial Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatSmallCard("Inventory Value", "$${String.format(Locale.getDefault(), "%.2f", stats.totalFoodCost)}", Modifier.weight(1f), Color(0xFF2196F3), Icons.Default.AccountBalanceWallet)
                    StatSmallCard("Wasted Value", "$${String.format(Locale.getDefault(), "%.2f", stats.moneyWasted)}", Modifier.weight(1f), MaterialTheme.colorScheme.error, Icons.Default.MoneyOff)
                }
            }

            item {
                Text("Waste Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatSmallCard("Waste Rate", "${String.format(Locale.getDefault(), "%.1f", stats.wastePercentage)}%", Modifier.weight(1f), if(stats.wastePercentage > 20) MaterialTheme.colorScheme.error else FoodGreen, Icons.Default.TrendingDown)
                    StatSmallCard("Saved Items", stats.savedCount.toString(), Modifier.weight(1f), FoodGreen, Icons.Default.Savings)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = FoodGreen)
                            Spacer(Modifier.width(12.dp))
                            Text("Most Wasted", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Category", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(stats.mostWastedCategory, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Item", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(stats.mostWastedFood, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (wasteRecords.isNotEmpty()) {
                item { Text("Waste History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(wasteRecords.take(5)) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("${record.category} • ${record.reason}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(record.quantity, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                if (record.price > 0) {
                                    Text("$${String.format(Locale.getDefault(), "%.2f", record.price)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun StatSmallCard(label: String, value: String, modifier: Modifier = Modifier, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
        }
    }
}
