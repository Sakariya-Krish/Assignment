package com.example.assignment.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.assignment.data.FoodStatus
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseFirstScreen(viewModel: FoodViewModel, onNavigateToDetails: (Int) -> Unit, onBack: () -> Unit) {
    val items by viewModel.allItems.collectAsState()
    
    val prioritized = items.filter { it.status == FoodStatus.FRESH }
        .sortedWith(compareBy<com.example.assignment.data.FoodItem> { it.expiryDate }
            .thenBy { it.name })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Use First", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (prioritized.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No fresh food items found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prioritized) { item ->
                    val daysLeft = ((item.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                    val statusText = when {
                        daysLeft < 0 -> "Expired"
                        daysLeft == 0 -> "Expires Today"
                        daysLeft == 1 -> "Expires Tomorrow"
                        else -> "Expires in $daysLeft days"
                    }
                    val statusColor = when {
                        daysLeft < 0 -> Color.Red
                        daysLeft <= 1 -> Color(0xFFFF9800)
                        else -> FoodGreen
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToDetails(item.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(statusText, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Text("View", color = FoodGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
