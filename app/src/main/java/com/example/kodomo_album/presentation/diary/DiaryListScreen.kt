package com.example.kodomo_album.presentation.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Diary
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(
    childId: String,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: DiaryListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childId) {
        viewModel.loadDiaries(childId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日記",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: Open date picker */ }
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "日付フィルタ")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate
            ) {
                Icon(Icons.Default.Add, contentDescription = "日記を作成")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchTextField(
                query = uiState.searchQuery,
                onQueryChange = { query ->
                    viewModel.searchDiaries(childId, query)
                },
                onClearClick = {
                    viewModel.clearSearch(childId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Date filter chip
            if (uiState.selectedDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        onClick = { viewModel.clearDateFilter(childId) },
                        label = {
                            Text(
                                uiState.selectedDate!!.format(
                                    DateTimeFormatter.ofPattern("MM月dd日")
                                )
                            )
                        },
                        selected = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "フィルタを削除",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.diaries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.isSearching) {
                                    "「${uiState.searchQuery}」に一致する日記が見つかりません"
                                } else {
                                    "まだ日記がありません"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.isSearching) {
                                    "別のキーワードで検索してみてください"
                                } else {
                                    "右下のボタンから日記を作成しましょう"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.diaries) { diary ->
                            DiaryListItem(
                                diary = diary,
                                onClick = { onNavigateToDetail(diary.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("日記を検索...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "検索")
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Clear, contentDescription = "クリア")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryListItem(
    diary: Diary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = diary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = diary.date.format(DateTimeFormatter.ofPattern("MM/dd")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = diary.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (diary.mediaIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search, // TODO: Change to photo icon
                        contentDescription = "写真",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${diary.mediaIds.size}件の写真・動画",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}