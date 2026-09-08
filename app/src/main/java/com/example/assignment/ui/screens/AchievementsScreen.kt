package com.example.assignment.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.assignment.ui.theme.FoodGreen
import com.example.assignment.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: FoodViewModel, onBack: () -> Unit) {
    val stats by viewModel.stats.collectAsState()
    
    // We can define all possible achievements and check if they are in the unlocked list from stats
    val allAchievements = listOf(
        AchievementUI("First Food Added", "Added your first item", "total", 1),
        AchievementUI("First Food Saved", "Consumed food before expiry", "saved", 1),
        AchievementUI("5 Foods Saved", "Saved 5 items from waste", "saved", 5),
        AchievementUI("10 Foods Saved", "Waste reduction pro!", "saved", 10),
        AchievementUI("Waste Reducer", "Keep waste percentage under 10%", "waste", 10)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allAchievements) { ach ->
                val isUnlocked = stats.achievements.any { it.title == ach.title }
                AchievementCard(ach, isUnlocked)
            }
        }
    }
}

@Composable
fun AchievementCard(ach: AchievementUI, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) FoodGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) FoodGreen else Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ach.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(ach.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isUnlocked) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = FoodGreen)
            }
        }
    }
}

data class AchievementUI(val title: String, val description: String, val type: String, val target: Int)
