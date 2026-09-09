package com.example.assignment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.data.FoodItem
import com.example.assignment.data.FoodStatus
import com.example.assignment.ui.theme.FoodGreen
import com.example.assignment.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryCalendarScreen(viewModel: FoodViewModel, onNavigateToDetails: (Int) -> Unit, onBack: () -> Unit) {
    val items by viewModel.allItems.collectAsState()
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val itemsOnSelectedDate = items.filter { 
        it.status == FoodStatus.FRESH && isSameDay(it.expiryDate, selectedDate.timeInMillis)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expiry Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.MONTH, -1)
                    selectedDate = newDate
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                }
                Text(sdfMonth.format(selectedDate.time), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { 
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.MONTH, 1)
                    selectedDate = newDate
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            // Simple Day Grid
            CalendarGrid(selectedDate, items) { day ->
                val newDate = selectedDate.clone() as Calendar
                newDate.set(Calendar.DAY_OF_MONTH, day)
                selectedDate = newDate
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Items for selected date
            Text(
                text = "Expiring on ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(selectedDate.time)}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (itemsOnSelectedDate.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items expiring on this day.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemsOnSelectedDate) { item ->
                        CalendarFoodItem(item, onClick = { onNavigateToDetails(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: Calendar, allItems: List<FoodItem>, onDaySelected: (Int) -> Unit) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val selectedDay = currentMonth.get(Calendar.DAY_OF_MONTH)
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val rows = (daysInMonth + 6) / 7
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 1..7) {
                    val day = i * 7 + j
                    if (day <= daysInMonth) {
                        val isSelected = day == selectedDay
                        val hasItems = allItems.any { 
                            it.status == FoodStatus.FRESH && isSameDay(it.expiryDate, getDateForDay(currentMonth, day)) 
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FoodGreen else Color.Transparent)
                                .clickable { onDaySelected(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                if (hasItems) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else FoodGreen))
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun getDateForDay(calendar: Calendar, day: Int): Long {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, day)
    return cal.timeInMillis
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun CalendarFoodItem(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("${item.category} • ${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("View", color = FoodGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
