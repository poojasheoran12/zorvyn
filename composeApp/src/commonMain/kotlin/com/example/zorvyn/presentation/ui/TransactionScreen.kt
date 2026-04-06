package com.example.zorvyn.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.presentation.*
import org.koin.compose.viewmodel.koinViewModel
import com.preat.peekaboo.image.picker.*
import com.preat.peekaboo.ui.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import com.example.zorvyn.util.ReceiptData
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            val scope = rememberCoroutineScope()
            TopAppBar(
                title = { Text("Transactions", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.exportToCsv().collect { csv ->
                                println("EXPORTED CSV:\n$csv")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Download, "Export CSV", tint = Color.White)
                    }
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.FilterList, "Filter", tint = Color.White) }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )

            FilterBar(
                currentFilter = uiState.currentFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            if (transactions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddTransactionBottomSheet(
            viewModel = viewModel,
            onDismiss = { showBottomSheet = false },
            onSave = { amount, note, type, category, receipt ->
                viewModel.addTransaction(amount, note, type, category, receipt)
                showBottomSheet = false
            }
        )
    }
}

@Composable
fun FilterBar(currentFilter: TransactionFilter, onFilterSelected: (TransactionFilter) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val filters = TransactionFilter.entries
        items(filters.toList()) { filter ->
            val isSelected = filter == currentFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { 
                    Text(
                        text = filter.name.replace("_", " ").lowercase().capitalize(),
                        fontSize = 13.sp,
                        fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00C853),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1E1E1E),
                    labelColor = Color.Gray
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.category.icon, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = transaction.formattedDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            val amountColor = if (transaction.type == TransactionType.INCOME) Color(0xFF00C853) else Color(0xFFFF5252)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ₹${transaction.amount.toInt()}",
                    color = amountColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search transactions...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFF2A2A2A),
            focusedBorderColor = Color(0xFF00C853),
            unfocusedContainerColor = Color(0xFF1E1E1E),
            focusedContainerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                "No Transactions",
                modifier = Modifier.size(60.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No transactions yet",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Tap + to add your first transaction",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onSave: (Double, String, TransactionType, TransactionCategory, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(TransactionCategory.FOOD) }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var isProcessingReceipt by remember { mutableStateOf(false) }

    val peekabooLauncher = rememberImagePickerLauncher(
        scope = scope,
        onResult = { images ->
            images.firstOrNull()?.let { imageBytes ->
                receiptPath = "local_receipt_uri" // Placeholder
                isProcessingReceipt = true
                scope.launch {
                    val data = viewModel.processReceipt(imageBytes)
                    data.amount?.let { amount = it.toString() }
                    data.note?.let { note = it }
                    isProcessingReceipt = false
                }
            }
        }
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Add Transaction", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (isProcessingReceipt) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00C853), strokeWidth = 2.dp)
                }
            }
            
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("₹ ") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    focusedLabelColor = Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .background(if(type == TransactionType.INCOME) Color(0xFF00C853) else Color.Transparent)
                        .clickable { type = TransactionType.INCOME },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .background(if(type == TransactionType.EXPENSE) Color(0xFFFF5252) else Color.Transparent)
                        .clickable { type = TransactionType.EXPENSE },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", fontWeight = FontWeight.Bold)
                }
            }
            
            // Category Selector
            Column {
                Text("Category", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.icon + " " + cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00C853),
                                containerColor = Color(0xFF2A2A2A)
                            ),
                            border = null
                        )
                    }
                }
            }
            
            // Receipt Section
            ReceiptUploadSection(
                receiptPath = receiptPath, 
                onPhotoAdded = { peekabooLauncher.launch() }, 
                onRemove = { receiptPath = null }
            )
            
            // Notes field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes / Merchant") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Save Button
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onSave(amt, if(note.isEmpty()) category.name else note, type, category, receiptPath) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Save Transaction", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReceiptUploadSection(receiptPath: String?, onPhotoAdded: () -> Unit, onRemove: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add Receipt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        
        if (receiptPath == null) {
            OutlinedButton(
                onClick = onPhotoAdded,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
            ) {
                Icon(Icons.Default.CameraAlt, "Camera")
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add Receipt", fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, "Receipt", modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Receipt Preview Loaded", color = Color.Gray, fontSize = 12.sp)
                }
                
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onPhotoAdded, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00C853))) {
                        Text("Change")
                    }
                    TextButton(onClick = onRemove, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}

// Utility extension
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
