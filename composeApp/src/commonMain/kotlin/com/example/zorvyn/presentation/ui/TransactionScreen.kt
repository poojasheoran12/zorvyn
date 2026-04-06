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
    onBack: () -> Unit = {},
    onAddTransaction: () -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            val scope = rememberCoroutineScope()
            TopAppBar(
                title = { Text("Transactions", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = {
                        viewModel.downloadData()
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
                onClick = onAddTransaction,
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = CircleShape
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

            if (uiState.isLoading) {
                LoadingState()
            } else if (transactions.isEmpty()) {
                EmptyState(
                    message = if (uiState.searchQuery.isNotEmpty()) 
                        "No transactions match your search" 
                    else "No transactions found for this filter"
                )
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
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                            onEdit = { onEditTransaction(transaction) }
                        )
                    }
                }
            }
        }
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




// End of TransactionScreen
