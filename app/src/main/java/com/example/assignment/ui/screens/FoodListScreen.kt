package com.example.assignment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    CategoryInfo("Other", Icons.Default.Category)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(viewModel: FoodViewModel, onNavigateToDetails: (Int) -> Unit) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredItems = items.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (it.name.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Food Inventory", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FoodGreen)
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            placeholder = { Text("Search food...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FoodGreen,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (items.isEmpty()) "Your inventory is empty." else "No items match your search.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->
                    FoodListItem(item, onClick = { onNavigateToDetails(item.id) })
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(60.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoriesList.find { it.name == item.category }?.icon ?: Icons.Default.Category,
                        contentDescription = null,
                        tint = FoodGreen
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("${item.category} • ${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            val daysLeft = ((item.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            
            val color = when {
                item.status == FoodStatus.CONSUMED -> Color.Gray
                item.status == FoodStatus.WASTED -> Color.Gray
                daysLeft < 0 -> Color.Red
                daysLeft < 3 -> Color(0xFFFF9800)
                else -> FoodGreen
            }

            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (daysLeft < 0) "Expired" else if (daysLeft == 0) "Today" else "$daysLeft d",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}
