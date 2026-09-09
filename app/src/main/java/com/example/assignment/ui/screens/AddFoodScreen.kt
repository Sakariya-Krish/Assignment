package com.example.assignment.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.assignment.data.FoodItem
import com.example.assignment.data.FoodStatus
import com.example.assignment.ui.components.SectionHeader
import com.example.assignment.util.DateUtils
import com.example.assignment.viewmodel.FoodViewModel
import com.example.assignment.ui.theme.FoodGreen
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    foodId: Int,
    viewModel: FoodViewModel,
    onSaved: () -> Unit,
    onNavigateToScanner: () -> Unit,
    scannedBarcode: String? = null,
    preName: String? = null,
    preCategory: String? = null,
    preQuantity: String? = null
) {
    val context = LocalContext.current
    val items by viewModel.allItems.collectAsState()
    val existingItem = remember(foodId, items) { items.find { it.id == foodId } }
    val recentItems = items.sortedByDescending { it.createdAt }.take(3)

    var name by remember(existingItem, preName) { mutableStateOf(existingItem?.name ?: preName ?: "") }
    var brand by remember(existingItem) { mutableStateOf(existingItem?.brand ?: "") }
    var category by remember(existingItem, preCategory) { mutableStateOf(existingItem?.category ?: preCategory ?: "Other") }
    var quantity by remember(existingItem, preQuantity) { mutableStateOf(existingItem?.quantity ?: preQuantity ?: "1") }
    var unit by remember(existingItem) { mutableStateOf(existingItem?.unit ?: "Pieces") }
    var price by remember(existingItem) { mutableStateOf(existingItem?.price?.let { if (it > 0) it.toString() else "" } ?: "") }
    var storageLocation by remember(existingItem) { mutableStateOf(existingItem?.storageLocation ?: "Refrigerator") }
    var purchaseDate by remember(existingItem) { mutableStateOf(existingItem?.purchaseDate ?: System.currentTimeMillis()) }
    var expiryDate by remember(existingItem) { mutableStateOf(existingItem?.expiryDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)) }
    var notes by remember(existingItem) { mutableStateOf(existingItem?.notes ?: "") }
    var imageUri by remember(existingItem) { mutableStateOf(existingItem?.imageUri) }
    var barcode by remember(existingItem, scannedBarcode) { mutableStateOf(scannedBarcode ?: existingItem?.barcode) }

    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Image Handlers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) imageUri = uri.toString() }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = tempImageUri.toString() }

    fun createImageUri(): Uri {
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "food_images")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "IMG_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter a food name") }
                        return@Button
                    }
                    if (expiryDate < purchaseDate) {
                        scope.launch { snackbarHostState.showSnackbar("Expiry date cannot be before purchase date") }
                        return@Button
                    }

                    isSaving = true
                    val item = FoodItem(
                        id = if (foodId == -1) 0 else foodId,
                        name = name.trim(),
                        brand = brand.trim(),
                        category = category,
                        quantity = quantity,
                        unit = unit,
                        price = price.toDoubleOrNull() ?: 0.0,
                        purchaseDate = purchaseDate,
                        expiryDate = expiryDate,
                        storageLocation = storageLocation,
                        notes = notes.trim(),
                        status = if (expiryDate < System.currentTimeMillis()) FoodStatus.EXPIRED else FoodStatus.FRESH,
                        imageUri = imageUri,
                        barcode = barcode,
                        createdAt = existingItem?.createdAt ?: System.currentTimeMillis()
                    )
                    
                    if (foodId == -1) viewModel.insert(item) else viewModel.update(item)
                    onSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FoodGreen),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(if (foodId == -1) Icons.Default.Add else Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (foodId == -1) "Add Food Item" else "Update Food Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                if (foodId == -1) "New Food Entry" else "Edit Food Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = FoodGreen
            )

            // 1. Food Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { imageUri = null }
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("Add Photo", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Barcode")
                }
                OutlinedButton(
                    onClick = {
                        val uri = createImageUri()
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            // 2. Food Information
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (Optional)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("$") },
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        listOf("Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Beverages", "Frozen", "Packaged", "Snacks", "Grains", "Other").forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Qty") },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(0.8f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        listOf("Pieces", "Kg", "Grams", "Litres", "ml", "Pack", "Bottle", "Box", "Other").forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                        }
                    }
                }
            }

            // 3. Date Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(purchaseDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Purchased") },
                    modifier = Modifier.weight(1f).clickable { showPurchaseDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(expiryDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expires *") },
                    modifier = Modifier.weight(1f).clickable { showExpiryDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (expiryDate < purchaseDate) Color.Red else MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }

            val daysLeft = DateUtils.getDaysRemaining(expiryDate).toInt()
            Surface(
                color = (if (daysLeft < 0) Color.Red else if (daysLeft < 3) Color(0xFFFF9800) else FoodGreen).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (daysLeft < 0) "Expired $daysLeft days ago" else "Expires in $daysLeft days",
                    color = if (daysLeft < 0) Color.Red else if (daysLeft < 3) Color(0xFFFF9800) else FoodGreen,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // 4. Storage Location
            Text("Storage Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val locations = listOf(
                    "Refrigerator" to Icons.Default.Kitchen,
                    "Freezer" to Icons.Default.AcUnit,
                    "Pantry" to Icons.Default.Inventory,
                    "Kitchen" to Icons.Default.Countertops,
                    "Cabinet" to Icons.Default.AllInbox,
                    "Other" to Icons.Default.MoreHoriz
                )
                items(locations) { (loc, icon) ->
                    StorageChip(
                        label = loc,
                        icon = icon,
                        isSelected = storageLocation == loc,
                        onClick = { storageLocation = loc }
                    )
                }
            }

            // 5. Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                placeholder = { Text("e.g. Keep away from sunlight...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Preview
            if (name.isNotBlank()) {
                SectionHeader("Smart Preview")
                ExpiryPreviewCard(name, category, quantity, unit, expiryDate)
            }

            // Recent History
            if (recentItems.isNotEmpty()) {
                SectionHeader("Recently Added")
                recentItems.forEach { item ->
                    RecentItemRow(item)
                }
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showPurchaseDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDate)
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDate = datePickerState.selectedDateMillis ?: purchaseDate
                    showPurchaseDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showExpiryDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiryDate)
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryDate = datePickerState.selectedDateMillis ?: expiryDate
                    showExpiryDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun StorageChip(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) FoodGreen else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) FoodGreen.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) FoodGreen else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = if (isSelected) FoodGreen else Color.Gray, style = MaterialTheme.typography.labelMedium, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun ExpiryPreviewCard(name: String, category: String, qty: String, unit: String, expiry: Long) {
    val daysLeft = DateUtils.getDaysRemaining(expiry).toInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("$category • $qty $unit", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                val color = if (daysLeft < 0) Color.Red else if (daysLeft < 3) Color(0xFFFF9800) else FoodGreen
                Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = if (daysLeft < 0) "Expired" else "Fresh",
                        color = color,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
                Text(if (daysLeft < 0) "Passed" else "In $daysLeft d", style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@Composable
fun RecentItemRow(item: FoodItem) {
    val daysLeft = DateUtils.getDaysRemaining(item.expiryDate).toInt()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.imageUri != null) {
            AsyncImage(
                model = item.imageUri,
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.size(44.dp).background(Color.LightGray.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.Gray)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text("Expires ${DateUtils.formatDisplayDate(item.expiryDate)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        val color = if (daysLeft < 0) Color.Red else if (daysLeft < 3) Color(0xFFFF9800) else FoodGreen
        Text(if (daysLeft < 0) "Expired" else "$daysLeft d", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}
