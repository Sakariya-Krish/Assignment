package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.assignment.ui.theme.FoodGreen
import com.example.assignment.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: FoodViewModel, onBack: () -> Unit) {
    val stats by viewModel.stats.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    
    val allAchievements = listOf(
        AchievementUI("First Food Added", "Added your first item", Icons.Default.Add),
        AchievementUI("First Food Saved", "Consumed food before expiry", Icons.Default.Savings),
        AchievementUI("5 Foods Saved", "Saved 5 items from waste", Icons.Default.AutoAwesome),
        AchievementUI("10 Foods Saved", "Waste reduction pro!", Icons.Default.EmojiEvents),
        AchievementUI("Waste Reducer", "Keep waste percentage under 10%", Icons.Default.TrendingDown)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals & Milestones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddGoalDialog = true }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Goal", tint = FoodGreen)
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically()
        ) {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                
                if (goals.isNotEmpty()) {
                    item { Text("Active Goals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(goals) { goal ->
                        GoalCard(goal)
                    }
                }

                item { Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(allAchievements) { ach ->
                    val isUnlocked = stats.achievements.any { it.title == ach.title }
                    AchievementCard(ach, isUnlocked)
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalTarget by remember { mutableStateOf("") }
        var goalType by remember { mutableStateOf("FOOD_SAVED") }
        
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Set a New Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = goalTitle, 
                        onValueChange = { goalTitle = it }, 
                        label = { Text("Goal Title (e.g. Save 10 items)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = goalTarget, 
                        onValueChange = { goalTarget = it }, 
                        label = { Text("Target Quantity") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    
                    Column {
                        Text("Goal Category", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = goalType == "FOOD_SAVED",
                                onClick = { goalType = "FOOD_SAVED" },
                                label = { Text("Food Saved") },
                                leadingIcon = if (goalType == "FOOD_SAVED") { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                            )
                            FilterChip(
                                selected = goalType == "WASTE_TRACKED",
                                onClick = { goalType = "WASTE_TRACKED" },
                                label = { Text("Waste Tracked") },
                                leadingIcon = if (goalType == "WASTE_TRACKED") { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = goalTarget.toFloatOrNull() ?: 0f
                        if (goalTitle.isNotBlank() && target > 0) {
                            viewModel.addGoal(goalTitle, target, goalType)
                            showAddGoalDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Create Goal") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun GoalCard(goal: com.example.assignment.data.Goal) {
    val progress = if (goal.targetValue > 0) goal.currentValue / goal.targetValue else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (goal.isCompleted) {
                    Surface(color = FoodGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text("COMPLETED", color = FoodGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (goal.isCompleted) FoodGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} units", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (goal.isCompleted) FoodGreen else Color.Unspecified)
            }
        }
    }
}

@Composable
fun AchievementCard(ach: AchievementUI, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) FoodGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, FoodGreen.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isUnlocked) FoodGreen.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isUnlocked) ach.icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) FoodGreen else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ach.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.Unspecified else Color.Gray
                )
                Text(
                    text = ach.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
            if (isUnlocked) {
                Icon(Icons.Default.Stars, contentDescription = "Unlocked", tint = FoodGreen, modifier = Modifier.size(24.dp))
            }
        }
    }
}

data class AchievementUI(val title: String, val description: String, val icon: ImageVector)
