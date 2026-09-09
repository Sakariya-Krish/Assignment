package com.example.assignment.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    var quickAddName by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredItems = remember(items, searchQuery, selectedCategory) {
        items.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
            (it.name.contains(searchQuery, ignoreCase = true) || it.notes.contains(searchQuery, ignoreCase = true))
        }
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
                        var showClearConfirm by remember { mutableStateOf(false) }
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Purchased", tint = MaterialTheme.colorScheme.error)
                        }
                        if (showClearConfirm) {
                            AlertDialog(
                                onDismissRequest = { showClearConfirm = false },
                                title = { Text("Clear Purchased?") },
                                text = { Text("Remove all completed items from your list?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.clearPurchasedShoppingItems()
                                        showClearConfirm = false
                                        scope.launch { snackbarHostState.showSnackbar("Purchased items cleared") }
                                    }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                                },
                                dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, 
                containerColor = FoodGreen,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            
            // Quick Add Input
            OutlinedTextField(
                value = quickAddName,
                onValueChange = { quickAddName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Quickly add something...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (quickAddName.isNotBlank()) {
                        viewModel.addShoppingItem(quickAddName, "1", "Pieces", "Other", "")
                        quickAddName = ""
                    }
                }),
                trailingIcon = {
                    if (quickAddName.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.addShoppingItem(quickAddName, "1", "Pieces", "Other", "")
                            quickAddName = ""
                        }) { Icon(Icons.Default.CheckCircle, tint = FoodGreen, contentDescription = "Add") }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FoodGreen)
            )

            AnimatedVisibility(visible = quickAddName.isEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    placeholder = { Text("Search list...") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            }

            LazyRow(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = listOf("All", "Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Beverages", "Frozen", "Packaged", "Snacks", "Grains", "Other")
                items(cats) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FoodGreen, selectedLabelColor = Color.White)
                    )
                }
            }

            // Progress Card
            if (items.isNotEmpty()) {
                val progress = if (items.isNotEmpty()) purchased.size.toFloat() / items.size else 0f
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Shopping Progress", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text("${purchased.size}/${items.size}", style = MaterialTheme.typography.labelMedium, color = FoodGreen)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
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
                        Text("Suggested Items", style = MaterialTheme.typography.titleSmall, color = FoodGreen, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(suggestions) { suggestion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FoodGreen.copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FoodGreen.copy(alpha = 0.1f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(suggestion.name, fontWeight = FontWeight.Bold)
                                    Text(suggestion.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Button(
                                    onClick = { viewModel.addShoppingItem(suggestion.name, suggestion.quantity, suggestion.unit, suggestion.category, "") },
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (toBuy.isNotEmpty()) {
                    item { Text("To Buy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    itemsIndexed(toBuy, key = { _, item -> item.id }) { _, item ->
                        ShoppingListItemV2(
                            item = item,
                            onToggle = { 
                                viewModel.toggleShoppingItem(item)
                                scope.launch { snackbarHostState.showSnackbar("${item.name} purchased!") }
                            },
                            onDelete = { 
                                scope.launch {
                                    viewModel.deleteShoppingItem(item)
                                    val result = snackbarHostState.showSnackbar("${item.name} deleted", actionLabel = "UNDO")
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
                    item { Text("Recently Purchased", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    itemsIndexed(purchased, key = { _, item -> item.id }) { _, item ->
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
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(Modifier.height(16.dp))
                                Text("No items to buy", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                                Text("Your shopping list is all clear!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Button(onClick = { showAddDialog = true }, modifier = Modifier.padding(top = 32.dp), shape = RoundedCornerShape(12.dp)) {
                                    Text("Add First Item")
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(100.dp)) }
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
            containerColor = if (item.isPurchased) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPurchased) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (item.isPurchased) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (item.isPurchased) FoodGreen else Color.Gray
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                        color = if (item.isPurchased) Color.Gray else Color.Unspecified
                    )
                    Text("${item.quantity} ${item.unit} • ${item.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp), tint = Color.Gray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                }
            }
            
            if (item.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(item.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 40.dp))
            }
            
            if (item.isPurchased && onAddToFood != null) {
                Button(
                    onClick = onAddToFood,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FoodGreen.copy(alpha = 0.1f), contentColor = FoodGreen),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Pantry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    var quantity by remember { mutableStateOf(item?.quantity ?: "1") }
    var unit by remember { mutableStateOf(item?.unit ?: "Pieces") }
    var category by remember { mutableStateOf(item?.category ?: "Other") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    
    val categories = listOf("Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Beverages", "Frozen", "Packaged", "Snacks", "Grains", "Other")
    val units = listOf("Pieces", "Kg", "Grams", "Litres", "ml", "Pack", "Bottle", "Box")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "New Shopping Item" else "Edit Shopping Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity, 
                        onValueChange = { quantity = it }, 
                        label = { Text("Qty") }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp), 
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    
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
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, quantity, unit, category, notes) }, shape = RoundedCornerShape(8.dp)) {
                Text(if (item == null) "Add Item" else "Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
