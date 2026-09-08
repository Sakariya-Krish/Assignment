package com.example.assignment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Waste Analysis v2.0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FoodGreen)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = FoodGreen)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Food Saving Score", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${stats.savingScore}", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Impact: ${if(stats.savingScore > 80) "Excellent" else "Good"}", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatSmallCard("Most Wasted Cat", stats.mostWastedCategory, Modifier.weight(1f), MaterialTheme.colorScheme.error)
                StatSmallCard("Most Wasted Food", stats.mostWastedFood, Modifier.weight(1f), MaterialTheme.colorScheme.error)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatSmallCard("Food Saved", stats.savedCount.toString(), Modifier.weight(1f), FoodGreen)
                StatSmallCard("Waste %", "${String.format(Locale.getDefault(), "%.1f", stats.wastePercentage)}%", Modifier.weight(1f), Color.Gray)
            }
        }

        if (stats.achievements.isNotEmpty()) {
            item { Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(stats.achievements) { achievement ->
                AchievementItem(achievement)
            }
        }

        if (wasteRecords.isNotEmpty()) {
            item { Text("Recent Waste History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(wasteRecords.take(5)) { record ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(record.foodName, fontWeight = FontWeight.Bold)
                            Text("${record.category} • ${record.reason}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(record.quantity, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: com.example.assignment.viewmodel.Achievement) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (achievement.isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                contentDescription = null,
                tint = if (achievement.isUnlocked) FoodGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(achievement.title, fontWeight = FontWeight.Bold)
                Text(achievement.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (achievement.isUnlocked) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FoodGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun StatSmallCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, maxLines = 1)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}
