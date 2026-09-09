package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.assignment.data.FoodItem
import com.example.assignment.data.FoodStatus
import com.example.assignment.util.DateUtils
import com.example.assignment.util.ExpiryStatus
import com.example.assignment.util.getExpiryStatus
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen

data class CategoryInfo(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
val categoriesList = listOf(
    CategoryInfo("All", Icons.Default.AllInclusive),
    CategoryInfo("Fruits", Icons.Default.Restaurant),
    CategoryInfo("Vegetables", Icons.Default.Eco),
    CategoryInfo("Dairy", Icons.Default.WaterDrop),
    CategoryInfo("Meat", Icons.Default.SetMeal),
    CategoryInfo("Bakery", Icons.Default.BakeryDining),
    CategoryInfo("Beverages", Icons.Default.LocalDrink),
    CategoryInfo("Frozen", Icons.Default.AcUnit),
    CategoryInfo("Packaged", Icons.Default.Inventory2),
    CategoryInfo("Snacks", Icons.Default.Fastfood),
    CategoryInfo("Grains", Icons.Default.Grain),
    CategoryInfo("Other", Icons.Default.Category)
)

enum class SortOption(val label: String) {
    EXPIRY_ASC("Expiry (Nearest)"),
    EXPIRY_DESC("Expiry (Furthest)"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    CREATED_DESC("Recently Added")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(viewModel: FoodViewModel, onNavigateToDetails: (Int) -> Unit) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf(SortOption.EXPIRY_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredAndSortedItems = remember(items, searchQuery, selectedCategory, selectedStatus, sortOption) {
        items.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.brand.contains(searchQuery, ignoreCase = true)
            
            val status = getExpiryStatus(item.expiryDate)
            val matchesStatus = when (selectedStatus) {
                "All" -> true
                "Fresh" -> status == ExpiryStatus.FRESH || status == ExpiryStatus.EXPIRING_SOON || status == ExpiryStatus.EXPIRING_TODAY
                "Expiring Soon" -> status == ExpiryStatus.EXPIRING_SOON || status == ExpiryStatus.EXPIRING_TODAY
                "Expired" -> status == ExpiryStatus.EXPIRED
                else -> true
            }
            
            matchesCategory && matchesSearch && matchesStatus
        }.sortedWith(when (sortOption) {
            SortOption.EXPIRY_ASC -> compareBy { it.expiryDate }
            SortOption.EXPIRY_DESC -> compareByDescending { it.expiryDate }
            SortOption.NAME_ASC -> compareBy { it.name.lowercase() }
            SortOption.NAME_DESC -> compareByDescending { it.name.lowercase() }
            SortOption.CREATED_DESC -> compareByDescending { it.createdAt }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Inventory", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    },
                                    trailingIcon = { if (sortOption == option) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("Search inventory...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FoodGreen,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )

            // Status Chips
            Row(
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("All", "Fresh", "Expiring Soon", "Expired")
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesList) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        leadingIcon = { Icon(cat.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoodGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            if (filteredAndSortedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (items.isEmpty()) "Your inventory is empty." else "No items match your search.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filteredAndSortedItems, key = { _, item -> item.id }) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it * (index + 1) / 10 },
                                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                            ) + fadeIn()
                        ) {
                            FoodListItem(item, onClick = { onNavigateToDetails(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListItem(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(70.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoriesList.find { it.name == item.category }?.icon ?: Icons.Default.Category,
                        contentDescription = null,
                        tint = FoodGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (item.brand.isNotBlank()) {
                    Text(item.brand, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text("${item.category} • ${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            val daysLeft = DateUtils.getDaysRemaining(item.expiryDate).toInt()
            
            val statusColor = when {
                item.status == FoodStatus.CONSUMED -> Color.Gray
                item.status == FoodStatus.WASTED -> Color.Gray
                daysLeft < 0 -> Color.Red
                daysLeft < 3 -> Color(0xFFFF9800)
                else -> FoodGreen
            }

            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (daysLeft < 0) "Expired" else if (daysLeft == 0) "Today" else "$daysLeft d",
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}
