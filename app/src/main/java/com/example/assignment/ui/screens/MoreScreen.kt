package com.example.assignment.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.assignment.ui.Screen
import com.example.assignment.ui.theme.FoodGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    val moreItems = listOf(
        MoreItem("Use First", "Priority food items", Icons.Default.PriorityHigh, Screen.UseFirst.route),
        MoreItem("Statistics", "Waste and impact analysis", Icons.Default.BarChart, Screen.Statistics.route),
        MoreItem("Food Assistant", "Get smart suggestions", Icons.Default.Assistant, Screen.FoodAssistant.route),
        MoreItem("Achievements", "Unlock saving goals", Icons.Default.Star, Screen.Achievements.route),
        MoreItem("Settings", "App preferences", Icons.Default.Settings, Screen.Settings.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("More", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(moreItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(item.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = FoodGreen.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = null, tint = FoodGreen)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}

data class MoreItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)
