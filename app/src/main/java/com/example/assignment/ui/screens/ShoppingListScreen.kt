package com.example.assignment.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.data.ShoppingItem
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: FoodViewModel, onNavigateToAddFood: (String, String, String) -> Unit) {
    val items by viewModel.allShoppingItems.collectAsState()
    val suggestions by viewModel.shoppingSuggestions.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredItems = items.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.notes.contains(searchQuery, ignoreCase = true))
    }

    val toBuy = filteredItems.filter { !it.isPurchased }
    val purchased = filteredItems.filter { it.isPurchased }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Shopping List", fontWeight = FontWeight.Bold) },
                actions = {
                    if (purchased.isNotEmpty()) {
                        IconButton(onClick = { 
                            scope.launch {
                                viewModel.clearPurchasedShoppingItems()
                                snackbarHostState.showSnackbar("Purchased items cleared")
                            }
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Purchased", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = FoodGreen) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()) {
            
            // Search and Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholder = { Text("Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = listOf("All", "Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Beverages", "Frozen", "Packaged", "Snacks", "Grains", "Other")
                items(cats) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FoodGreen, selectedLabelColor = Color.White)
                    )
                }
            }

            // Progress
            if (items.isNotEmpty()) {
                val progress = if (items.isNotEmpty()) purchased.size.toFloat() / items.size else 0f
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", style = MaterialTheme.typography.labelMedium)
                            Text("${purchased.size} of ${items.size} purchased", style = MaterialTheme.typography.labelSmall)
                        }
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                            color = FoodGreen,
                            trackColor = FoodGreen.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                
                // Smart Suggestions
                if (suggestions.isNotEmpty() && toBuy.isEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text("Suggested for you", style = MaterialTheme.typography.titleSmall, color = FoodGreen, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(suggestions) { suggestion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = FoodGreen.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(suggestion.name, fontWeight = FontWeight.Bold)
                                    Text(suggestion.category, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { viewModel.addShoppingItem(suggestion.name, suggestion.quantity, suggestion.unit, suggestion.category, "") },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (toBuy.isNotEmpty()) {
                    item { Text("To Buy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(toBuy) { item ->
                        ShoppingListItemV2(
                            item = item,
                            onToggle = { viewModel.toggleShoppingItem(item) },
                            onDelete = { 
                                scope.launch {
                                    viewModel.deleteShoppingItem(item)
                                    val result = snackbarHostState.showSnackbar("Item deleted", actionLabel = "UNDO")
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addShoppingItem(item.name, item.quantity, item.unit, item.category, item.notes)
                                    }
                                }
                            },
                            onEdit = { editingItem = item }
                        )
                    }
                }

                if (purchased.isNotEmpty()) {
                    item { Text("Purchased", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(purchased) { item ->
                        ShoppingListItemV2(
                            item = item,
                            onToggle = { viewModel.toggleShoppingItem(item) },
                            onDelete = { viewModel.deleteShoppingItem(item) },
                            onEdit = { editingItem = item },
                            onAddToFood = { onNavigateToAddFood(item.name, item.category, item.quantity) }
                        )
                    }
                }

                if (items.isEmpty() && suggestions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Text("Your shopping list is empty", color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text("Add items you need to buy.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Button(onClick = { showAddDialog = true }, modifier = Modifier.padding(top = 16.dp)) {
                                    Text("+ Add Item")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingItem != null) {
        ShoppingItemDialog(
            item = editingItem,
            onDismiss = { 
                showAddDialog = false
                editingItem = null
            },
            onConfirm = { name, qty, unit, cat, notes ->
                if (editingItem != null) {
                    viewModel.updateShoppingItem(editingItem!!.copy(name = name, quantity = qty, unit = unit, category = cat, notes = notes))
                } else {
                    viewModel.addShoppingItem(name, qty, unit, cat, notes)
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
fun ShoppingListItemV2(
    item: ShoppingItem, 
    onToggle: () -> Unit, 
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddToFood: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) Color.LightGray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isPurchased, onCheckedChange = { onToggle() })
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                    )
                    Text("${item.quantity} ${item.unit} • ${item.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
            }
            
            if (item.notes.isNotBlank()) {
                Text(item.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 48.dp, bottom = 8.dp))
            }
            
            if (item.isPurchased && onAddToFood != null) {
                Button(
                    onClick = onAddToFood,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FoodGreen.copy(alpha = 0.1f), contentColor = FoodGreen),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add to My Food", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemDialog(
    item: ShoppingItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "Pieces") }
    var category by remember { mutableStateOf(item?.category ?: "Other") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    
    val categories = listOf("Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Beverages", "Frozen", "Packaged", "Snacks", "Grains", "Other")
    val units = listOf("Pieces", "Kg", "Grams", "Litres", "ml", "Pack", "Bottle", "Box")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "New Item" else "Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }, modifier = Modifier.weight(1.5f)) {
                        OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            units.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                        }
                    }
                }

                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; catExpanded = false }) }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, quantity, unit, category, notes) }) {
                Text(if (item == null) "Add Item" else "Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
