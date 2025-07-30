package com.example.kodomoalbum.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kodomoalbum.data.model.DateRange
import com.example.kodomoalbum.data.model.SearchFilter
import com.example.kodomoalbum.data.model.SearchResultType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    initialFilter: SearchFilter,
    onFilterApply: (SearchFilter) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentFilter by remember { mutableStateOf(initialFilter) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "高度な検索",
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                TextButton(onClick = onNavigateBack) {
                    Text("キャンセル")
                }
                Button(
                    onClick = { onFilterApply(currentFilter) }
                ) {
                    Text("適用")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // コンテンツタイプフィルター
            item {
                FilterSection(
                    title = "コンテンツタイプ",
                    icon = Icons.Default.Category
                ) {
                    ContentTypeFilter(
                        selectedTypes = currentFilter.types,
                        onTypesChange = { types ->
                            currentFilter = currentFilter.copy(types = types)
                        }
                    )
                }
            }
            
            // 日付範囲フィルター
            item {
                FilterSection(
                    title = "日付範囲",
                    icon = Icons.Default.DateRange
                ) {
                    DateRangeFilter(
                        dateRange = currentFilter.dateRange,
                        onDateRangeChange = { dateRange ->
                            currentFilter = currentFilter.copy(dateRange = dateRange)
                        },
                        onShowDatePicker = { target ->
                            datePickerTarget = target
                            showDatePicker = true
                        }
                    )
                }
            }
            
            // カテゴリフィルター
            item {
                FilterSection(
                    title = "カテゴリ",
                    icon = Icons.Default.Label
                ) {
                    CategoryFilter(
                        selectedCategories = currentFilter.categories,
                        onCategoriesChange = { categories ->
                            currentFilter = currentFilter.copy(categories = categories)
                        }
                    )
                }
            }
            
            // タグフィルター
            item {
                FilterSection(
                    title = "タグ",
                    icon = Icons.Default.Tag
                ) {
                    TagFilter(
                        selectedTags = currentFilter.tags,
                        onTagsChange = { tags ->
                            currentFilter = currentFilter.copy(tags = tags)
                        }
                    )
                }
            }
            
            // フィルターリセット
            item {
                OutlinedButton(
                    onClick = {
                        currentFilter = SearchFilter()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("フィルターをリセット")
                }
            }
        }
    }
    
    // 日付選択ダイアログ
    if (showDatePicker && datePickerTarget != null) {
        DatePickerDialog(
            onDateSelected = { date ->
                when (datePickerTarget) {
                    DatePickerTarget.START_DATE -> {
                        val newDateRange = if (currentFilter.dateRange != null) {
                            currentFilter.dateRange!!.copy(startDate = date)
                        } else {
                            DateRange(startDate = date, endDate = LocalDate.now())
                        }
                        currentFilter = currentFilter.copy(dateRange = newDateRange)
                    }
                    DatePickerTarget.END_DATE -> {
                        val newDateRange = if (currentFilter.dateRange != null) {
                            currentFilter.dateRange!!.copy(endDate = date)
                        } else {
                            DateRange(startDate = LocalDate.now().minusMonths(1), endDate = date)
                        }
                        currentFilter = currentFilter.copy(dateRange = newDateRange)
                    }
                    null -> {}
                }
                showDatePicker = false
                datePickerTarget = null
            },
            onDismiss = {
                showDatePicker = false
                datePickerTarget = null
            }
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ContentTypeFilter(
    selectedTypes: List<SearchResultType>,
    onTypesChange: (List<SearchResultType>) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup()
    ) {
        SearchResultType.values().forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedTypes.contains(type),
                    onCheckedChange = { checked ->
                        val newTypes = if (checked) {
                            selectedTypes + type
                        } else {
                            selectedTypes - type
                        }
                        onTypesChange(newTypes)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getTypeDisplayName(type),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilter(
    dateRange: DateRange?,
    onDateRangeChange: (DateRange?) -> Unit,
    onShowDatePicker: (DatePickerTarget) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                onClick = { onShowDatePicker(DatePickerTarget.START_DATE) },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "開始日",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = dateRange?.startDate?.format(formatter) ?: "未設定",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            OutlinedCard(
                onClick = { onShowDatePicker(DatePickerTarget.END_DATE) },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "終了日",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = dateRange?.endDate?.format(formatter) ?: "未設定",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        if (dateRange != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onDateRangeChange(null) }
            ) {
                Text("日付範囲をクリア")
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategories: List<String>,
    onCategoriesChange: (List<String>) -> Unit
) {
    val availableCategories = listOf(
        "日常", "イベント", "成長記録", "初めて", "外出", "お風呂", "食事", "睡眠"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableCategories.chunked(2).forEach { categoryPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryPair.forEach { category ->
                    FilterChip(
                        onClick = {
                            val newCategories = if (selectedCategories.contains(category)) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                            onCategoriesChange(newCategories)
                        },
                        label = { Text(category) },
                        selected = selectedCategories.contains(category),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (categoryPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TagFilter(
    selectedTags: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    val availableTags = listOf(
        "可愛い", "成長", "初めて", "お気に入り", "記念", "面白い", "感動"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableTags.chunked(3).forEach { tagGroup ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tagGroup.forEach { tag ->
                    FilterChip(
                        onClick = {
                            val newTags = if (selectedTags.contains(tag)) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                            onTagsChange(newTags)
                        },
                        label = { Text(tag) },
                        selected = selectedTags.contains(tag),
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - tagGroup.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private enum class DatePickerTarget {
    START_DATE, END_DATE
}

private fun getTypeDisplayName(type: SearchResultType): String {
    return when (type) {
        SearchResultType.DIARY -> "日記"
        SearchResultType.MEDIA -> "写真・動画"
        SearchResultType.EVENT -> "イベント"
        SearchResultType.MILESTONE -> "発達記録"
    }
}