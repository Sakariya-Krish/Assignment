package com.example.assignment.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.assignment.data.FoodStatus
import com.example.assignment.util.DateUtils
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    foodId: Int, 
    viewModel: FoodViewModel, 
    onBack: () -> Unit, 
    onEdit: (Int) -> Unit,
    onNavigateToShopping: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    val item = items.find { it.id == foodId }
    
    var showWasteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val wasteReasons = listOf("Expired", "Spoiled", "Too much food", "Not liked", "Other")
    var selectedReason by remember { mutableStateOf(wasteReasons[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    item?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        item?.let { food ->
            Column(
                modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (food.imageUri != null) {
                    AsyncImage(
                        model = food.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        if (food.brand.isNotBlank()) {
                            Text(food.brand, style = MaterialTheme.typography.titleMedium, color = FoodGreen)
                        }
                        Text(food.category, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                    }
                    
                    val daysLeft = DateUtils.getDaysRemaining(food.expiryDate).toInt()
                    val color = when {
                        food.status == FoodStatus.CONSUMED -> Color.Gray
                        food.status == FoodStatus.WASTED -> Color.Gray
                        daysLeft < 0 -> Color.Red
                        daysLeft < 3 -> Color(0xFFFF9800)
                        else -> FoodGreen
                    }
                    
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = food.status.name,
                            color = color,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                val daysLeft = DateUtils.getDaysRemaining(food.expiryDate).toInt()
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Quantity", "${food.quantity} ${food.unit}")
                        if (food.price > 0) DetailRow("Price", "$${String.format(Locale.getDefault(), "%.2f", food.price)}")
                        DetailRow("Storage", food.storageLocation)
                        DetailRow("Purchase Date", DateUtils.formatDisplayDate(food.purchaseDate))
                        DetailRow("Expiry Date", DateUtils.formatDisplayDate(food.expiryDate))
                        DetailRow("Time Remaining", if (daysLeft < 0) "Expired" else "$daysLeft days left")
                        if (!food.barcode.isNullOrBlank()) DetailRow("Barcode", food.barcode)
                        DetailRow("Notes", food.notes.ifEmpty { "No notes provided" })
                    }
                }

                if (food.status == FoodStatus.FRESH) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                viewModel.markAsConsumed(food)
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FoodGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mark Consumed")
                        }
                        Button(
                            onClick = { showWasteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Mark Wasted")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { 
                            viewModel.addShoppingItem(food.name, food.quantity, food.unit, food.category, "")
                            onNavigateToShopping()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Shopping List")
                    }

                    Button(
                        onClick = { viewModel.update(food.copy(status = FoodStatus.FRESH)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Restore to Fresh")
                    }
                }
            }
        }
    }

    if (showWasteDialog) {
        AlertDialog(
            onDismissRequest = { showWasteDialog = false },
            title = { Text("Reason for Waste") },
            text = {
                Column {
                    wasteReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedReason = reason }.padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                            Text(reason, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    item?.let { viewModel.markAsWasted(it, selectedReason) }
                    showWasteDialog = false
                    onBack()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showWasteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Food?") },
            text = { Text("Are you sure you want to remove this item permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    item?.let { viewModel.delete(it) }
                    showDeleteConfirm = false
                    onBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}
