package com.example.kodomoalbum.presentation.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomoalbum.data.model.DateRange
import com.example.kodomoalbum.data.model.ExportProgress
import com.example.kodomoalbum.data.model.ExportRequest
import com.example.kodomoalbum.data.model.ExportType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExportScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    
    LaunchedEffect(childId) {
        viewModel.updateExportRequest(
            uiState.exportRequest.copy(childId = childId)
        )
    }
    
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
                text = "データエクスポート",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "閉じる")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        when {
            uiState.isExporting -> {
                ExportProgressContent(
                    progress = exportProgress,
                    onCancel = {
                        viewModel.resetExport()
                        onNavigateBack()
                    }
                )
            }
            
            uiState.isCompleted -> {
                ExportCompletedContent(
                    onReset = viewModel::resetExport,
                    onClose = onNavigateBack
                )
            }
            
            else -> {
                ExportConfigurationContent(
                    request = uiState.exportRequest,
                    onRequestUpdate = viewModel::updateExportRequest,
                    onStartExport = viewModel::startExport,
                    error = uiState.error,
                    onClearError = viewModel::clearError
                )
            }
        }
    }
}

@Composable
private fun ExportConfigurationContent(
    request: ExportRequest,
    onRequestUpdate: (ExportRequest) -> Unit,
    onStartExport: () -> Unit,
    error: String?,
    onClearError: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // エクスポート形式選択
        ExportTypeSection(
            selectedType = request.exportType,
            onTypeChange = { type ->
                onRequestUpdate(request.copy(exportType = type))
            }
        )
        
        // 日付範囲選択
        DateRangeSection(
            dateRange = request.dateRange,
            onDateRangeChange = { dateRange ->
                onRequestUpdate(request.copy(dateRange = dateRange))
            }
        )
        
        // コンテンツ選択
        ContentSelectionSection(
            request = request,
            onRequestUpdate = onRequestUpdate
        )
        
        // エラー表示
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    IconButton(onClick = onClearError) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // エクスポート開始ボタン
        Button(
            onClick = onStartExport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("エクスポート開始")
        }
    }
}

@Composable
private fun ExportTypeSection(
    selectedType: ExportType,
    onTypeChange: (ExportType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "エクスポート形式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                ExportType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = getExportTypeDisplayName(type),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getExportTypeDescription(type),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (type != ExportType.values().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeSection(
    dateRange: DateRange?,
    onDateRangeChange: (DateRange?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "日付範囲",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = dateRange != null,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onDateRangeChange(
                                DateRange(
                                    startDate = LocalDate.now().minusMonths(1),
                                    endDate = LocalDate.now()
                                )
                            )
                        } else {
                            onDateRangeChange(null)
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "日付範囲を指定する",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (dateRange != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                Text(
                    text = "${dateRange.startDate.format(formatter)} ～ ${dateRange.endDate.format(formatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun ContentSelectionSection(
    request: ExportRequest,
    onRequestUpdate: (ExportRequest) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "エクスポートするコンテンツ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ContentCheckboxItem(
                text = "写真・動画",
                checked = request.includePhotos,
                onCheckedChange = { checked ->
                    onRequestUpdate(request.copy(includePhotos = checked))
                }
            )
            
            ContentCheckboxItem(
                text = "日記",
                checked = request.includeDiaries,
                onCheckedChange = { checked ->
                    onRequestUpdate(request.copy(includeDiaries = checked))
                }
            )
            
            ContentCheckboxItem(
                text = "イベント",
                checked = request.includeEvents,
                onCheckedChange = { checked ->
                    onRequestUpdate(request.copy(includeEvents = checked))
                }
            )
            
            ContentCheckboxItem(
                text = "発達記録",
                checked = request.includeMilestones,
                onCheckedChange = { checked ->
                    onRequestUpdate(request.copy(includeMilestones = checked))
                }
            )
            
            ContentCheckboxItem(
                text = "成長記録",
                checked = request.includeGrowthRecords,
                onCheckedChange = { checked ->
                    onRequestUpdate(request.copy(includeGrowthRecords = checked))
                }
            )
        }
    }
}

@Composable
private fun ContentCheckboxItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExportProgressContent(
    progress: ExportProgress?,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = progress?.progress ?: 0f,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = progress?.currentStep ?: "処理中...",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        if (progress != null && progress.progress > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(progress.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(onClick = onCancel) {
            Text("キャンセル")
        }
    }
}

@Composable
private fun ExportCompletedContent(
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "エクスポート完了",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "ファイルがダウンロードフォルダに保存されました",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onReset) {
                Text("再度エクスポート")
            }
            Button(onClick = onClose) {
                Text("完了")
            }
        }
    }
}

private fun getExportTypeDisplayName(type: ExportType): String {
    return when (type) {
        ExportType.PDF -> "PDF"
        ExportType.BACKUP_JSON -> "バックアップ（JSON）"
    }
}

private fun getExportTypeDescription(type: ExportType): String {
    return when (type) {
        ExportType.PDF -> "印刷可能なPDF形式で出力"
        ExportType.BACKUP_JSON -> "データバックアップ用のJSON形式で出力"
    }
}