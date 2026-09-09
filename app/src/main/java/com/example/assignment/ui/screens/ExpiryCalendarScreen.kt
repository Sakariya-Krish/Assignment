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
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.example.assignment.ui.components.EmptyState
import com.example.assignment.ui.components.SectionHeader
import com.example.assignment.ui.theme.FoodGreen
import com.example.assignment.util.DateUtils
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
        it.status == FoodStatus.FRESH && DateUtils.isSameDay(it.expiryDate, selectedDate.timeInMillis)
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

            CalendarGrid(selectedDate, items) { day ->
                val newDate = selectedDate.clone() as Calendar
                newDate.set(Calendar.DAY_OF_MONTH, day)
                selectedDate = newDate
            }

            Divider(modifier = Modifier.padding(top = 16.dp), thickness = 0.5.dp)

            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader("Expiring on ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(selectedDate.time)}")

                if (itemsOnSelectedDate.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "Clear Day",
                        subtitle = "No items expiring on this date.",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(itemsOnSelectedDate) { item ->
                            CalendarFoodItem(item, onClick = { onNavigateToDetails(item.id) })
                        }
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
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        val rows = (daysInMonth + 6) / 7
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 1..7) {
                    val day = i * 7 + j
                    if (day <= daysInMonth) {
                        val isSelected = day == selectedDay
                        val dateForDay = Calendar.getInstance().apply {
                            timeInMillis = currentMonth.timeInMillis
                            set(Calendar.DAY_OF_MONTH, day)
                        }.timeInMillis
                        
                        val hasItems = allItems.any { 
                            it.status == FoodStatus.FRESH && DateUtils.isSameDay(it.expiryDate, dateForDay) 
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FoodGreen else Color.Transparent)
                                .clickable { onDaySelected(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasItems) {
                                    Box(modifier = Modifier.padding(top = 2.dp).size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else FoodGreen))
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

@Composable
fun CalendarFoodItem(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("${item.category} • ${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("Details", color = FoodGreen, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}
