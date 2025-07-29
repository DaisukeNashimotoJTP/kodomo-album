package com.example.kodomo_album.presentation.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomo_album.core.util.UiEvent
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryCreateEditScreen(
    childId: String,
    diaryId: String? = null,
    onNavigateUp: () -> Unit,
    onSelectMedia: () -> Unit,
    viewModel: DiaryCreateEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load diary if editing
    LaunchedEffect(diaryId) {
        if (diaryId != null) {
            viewModel.loadDiary(diaryId)
        }
    }

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is UiEvent.NavigateUp -> {
                    onNavigateUp()
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
                        text = if (uiState.isEditMode) "日記を編集" else "日記を作成",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(
                            onClick = { viewModel.deleteDiary() },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "削除")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.saveDiary(childId) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("タイトル") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Date selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Open date picker */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "日付",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "日付",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = uiState.selectedDate.format(
                                    DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Content input
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = viewModel::updateContent,
                    label = { Text("内容") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )

                // Media selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSelectMedia
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "写真",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "写真・動画",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = if (uiState.selectedMediaIds.isEmpty()) {
                                    "タップして写真・動画を選択"
                                } else {
                                    "${uiState.selectedMediaIds.size}件選択中"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Selected media preview (if any)
                if (uiState.selectedMediaIds.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "選択した写真・動画",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            uiState.selectedMediaIds.forEach { mediaId ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "メディア: $mediaId",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    TextButton(
                                        onClick = { viewModel.removeMedia(mediaId) }
                                    ) {
                                        Text("削除")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}