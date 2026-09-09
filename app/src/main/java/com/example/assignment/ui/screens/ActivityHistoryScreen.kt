package com.example.assignment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.assignment.ui.components.EmptyState
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(viewModel: FoodViewModel, onBack: () -> Unit) {
    val history by viewModel.activityHistory.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Timeline", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No activity yet",
                subtitle = "Tracked events will appear here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { record ->
                    ActivityItemRow(record, sdf.format(Date(record.timestamp)))
                }
            }
        }
    }
}

@Composable
fun ActivityItemRow(record: com.example.assignment.data.ActivityRecord, time: String) {
    val (icon, color) = when (record.action) {
        "ADDED" -> Icons.Default.AddCircle to FoodGreen
        "CONSUMED" -> Icons.Default.CheckCircle to FoodGreen
        "WASTED" -> Icons.Default.DeleteOutline to Color.Red
        "EXPIRED" -> Icons.Default.WarningAmber to Color.Red
        "PURCHASED" -> Icons.Default.ShoppingCart to Color(0xFF2196F3)
        "ADDED_TO_SHOPPING" -> Icons.Default.FormatListBulleted to Color.Gray
        "RESTORED" -> Icons.Default.SettingsBackupRestore to FoodGreen
        else -> Icons.Default.Info to Color.Gray
    }

    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f)))
        }
        
        Spacer(Modifier.width(16.dp))
        
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(record.foodName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                    Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Text(record.details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
