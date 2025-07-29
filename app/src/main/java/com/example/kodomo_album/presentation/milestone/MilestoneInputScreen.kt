package com.example.kodomo_album.presentation.milestone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.MilestoneType
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneInputScreen(
    childId: String,
    onNavigateUp: () -> Unit,
    viewModel: MilestoneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    LaunchedEffect(childId) {
        viewModel.loadMilestones(childId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateUp -> onNavigateUp()
                is UiEvent.ShowSnackbar -> {
                    // Snackbarの表示処理
                }
                is UiEvent.Navigate -> {
                    // Navigate処理
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // トップバー
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
            }
            Text(
                text = "発達記録を追加",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // タイトル入力
        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("タイトル") },
            placeholder = { Text("例: 初めて歩いた") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 説明入力
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("説明") },
            placeholder = { Text("詳細な説明を入力してください") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 発達タイプ選択
        Text(
            text = "発達タイプ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            MilestoneType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (uiState.selectedType == type),
                            onClick = { viewModel.onTypeChanged(type) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (uiState.selectedType == type),
                        onClick = null
                    )
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 達成日選択
        OutlinedTextField(
            value = uiState.achievedAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            onValueChange = { },
            label = { Text("達成日") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { 
                    // DatePickerの実装が必要
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "日付選択")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 保存ボタン
        Button(
            onClick = { viewModel.createMilestone() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("保存")
            }
        }
    }
}