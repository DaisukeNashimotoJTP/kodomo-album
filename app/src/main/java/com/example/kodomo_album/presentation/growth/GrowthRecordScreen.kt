package com.example.kodomo_album.presentation.growth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthRecordScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChart: (String) -> Unit = {},
    onNavigateToSummary: (String) -> Unit = {},
    viewModel: GrowthRecordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(childId) {
        viewModel.loadGrowthHistory(childId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.loadGrowthHistory(childId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ヘッダー
        Text(
            text = if (state.editingRecord != null) "成長記録編集" else "成長記録入力",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ナビゲーションボタン
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToChart(childId) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("グラフ")
            }
            OutlinedButton(
                onClick = { onNavigateToSummary(childId) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("レポート")
            }
        }

        // エラー表示
        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // 測定日選択
        OutlinedTextField(
            value = state.recordedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            onValueChange = { },
            label = { Text("測定日") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "日付選択")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 身長入力
        OutlinedTextField(
            value = state.height,
            onValueChange = viewModel::updateHeight,
            label = { Text("身長 (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 体重入力
        OutlinedTextField(
            value = state.weight,
            onValueChange = viewModel::updateWeight,
            label = { Text("体重 (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 頭囲入力
        OutlinedTextField(
            value = state.headCircumference,
            onValueChange = viewModel::updateHeadCircumference,
            label = { Text("頭囲 (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // メモ入力
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::updateNotes,
            label = { Text("メモ（任意）") },
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // ボタン行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // キャンセル/戻るボタン
            OutlinedButton(
                onClick = {
                    if (state.editingRecord != null) {
                        viewModel.clearForm()
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.editingRecord != null) "キャンセル" else "戻る")
            }

            // 保存ボタン
            Button(
                onClick = { viewModel.saveGrowthRecord(childId) },
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (state.editingRecord != null) "更新" else "保存")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 記録履歴
        Text(
            text = "記録履歴",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.growthRecords.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "まだ記録がありません",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            state.growthRecords.forEach { record ->
                GrowthRecordItem(
                    record = record,
                    onEdit = { viewModel.editGrowthRecord(record) },
                    onDelete = { viewModel.deleteGrowthRecord(record.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // 日付選択ダイアログ
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = state.recordedDate,
            onDateSelected = { date ->
                viewModel.updateRecordedDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}