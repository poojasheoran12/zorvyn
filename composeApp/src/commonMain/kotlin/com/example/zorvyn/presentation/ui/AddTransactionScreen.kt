package com.example.zorvyn.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.presentation.TransactionViewModel
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = koinViewModel(),
    existingTransaction: Transaction? = null,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    var amount by remember { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var type by remember { mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(existingTransaction?.category ?: TransactionCategory.FOOD) }
    var receiptPath by remember { mutableStateOf<String?>(existingTransaction?.receiptPath) }
    var isProcessingReceipt by remember { mutableStateOf(false) }

    val peekabooLauncher = rememberImagePickerLauncher(
        scope = scope,
        onResult = { images ->
            images.firstOrNull()?.let { imageBytes ->
                receiptPath = "local_uri"
                isProcessingReceipt = true
                scope.launch {
                    val data = viewModel.processReceipt(imageBytes)
                    data.amount?.let { amount = it.toString() }
                    data.note?.let { note = it }
                    data.type?.let { type = it }
                    isProcessingReceipt = false
                }
            }
        }
    )

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if(existingTransaction == null) "New Transaction" else "Edit Transaction", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Cancel", tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("How much?", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    placeholder = { Text("₹0", color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E1E1E))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if(type == TransactionType.INCOME) Color(0xFF00C853) else Color.Transparent)
                        .clickable { type = TransactionType.INCOME },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", fontWeight = FontWeight.Bold, color = if(type == TransactionType.INCOME) Color.White else Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if(type == TransactionType.EXPENSE) Color(0xFFFF5252) else Color.Transparent)
                        .clickable { type = TransactionType.EXPENSE },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", fontWeight = FontWeight.Bold, color = if(type == TransactionType.EXPENSE) Color.White else Color.Gray)
                }
            }

            // Category Selection
            Column {
                Text("Category", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(TransactionCategory.entries.toList()) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.icon + " " + cat.name.lowercase().capitalize()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00C853).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00C853),
                                containerColor = Color(0xFF1E1E1E),
                                labelColor = Color.Gray
                            ),
                            border = BorderStroke(1.dp, if(category == cat) Color(0xFF00C853) else Color.Transparent),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Receipt Section
            Card(
                modifier = Modifier.fillMaxWidth().clickable { peekabooLauncher.launch() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, "Scan", tint = Color(0xFF00C853), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Add Receipt / Scan", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if(receiptPath == null) "Use AI to auto-fill details" else "Receipt Attached ✅", color = Color.Gray, fontSize = 12.sp)
                    }
                    if (isProcessingReceipt) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF00C853))
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What for? (Notes, Brand, Merchant)", color = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF00C853)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (existingTransaction != null) {
                        viewModel.updateTransaction(
                            existingTransaction.copy(
                                amount = amt,
                                note = note.ifEmpty { category.name },
                                type = type,
                                category = category,
                                receiptPath = receiptPath
                            )
                        )
                    } else {
                        viewModel.addTransaction(amt, note.ifEmpty { category.name }, type, category, receiptPath)
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Confirm Transaction", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun String.capitalize() = lowercase().replaceFirstChar { it.uppercase() }
