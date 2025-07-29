package com.example.kodomo_album.presentation.growth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kodomo_album.domain.model.GrowthPeriod
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthSummaryScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    onExportData: () -> Unit = {},
    viewModel: GrowthSummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadGrowthSummary(childId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // トップバー
        TopAppBar(
            title = { Text("成長レポート") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                }
            },
            actions = {
                IconButton(onClick = onExportData) {
                    Icon(Icons.Default.Share, contentDescription = "エクスポート")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 期間選択
            PeriodSelectionCard(
                selectedPeriod = state.selectedPeriod,
                onPeriodChanged = viewModel::setPeriod
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // ローディング表示
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // サマリー表示
                state.summary?.let { summary ->
                    GrowthSummaryCard(summary = summary)
                } ?: run {
                    // データがない場合
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "成長データがありません",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "成長記録を追加してレポートを表示しましょう",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelectionCard(
    selectedPeriod: GrowthPeriod,
    onPeriodChanged: (GrowthPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "レポート期間",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodChip(
                    text = "1ヶ月",
                    selected = selectedPeriod == GrowthPeriod.MONTH,
                    onClick = { onPeriodChanged(GrowthPeriod.MONTH) }
                )
                PeriodChip(
                    text = "3ヶ月",
                    selected = selectedPeriod == GrowthPeriod.THREE_MONTHS,
                    onClick = { onPeriodChanged(GrowthPeriod.THREE_MONTHS) }
                )
                PeriodChip(
                    text = "6ヶ月",
                    selected = selectedPeriod == GrowthPeriod.SIX_MONTHS,
                    onClick = { onPeriodChanged(GrowthPeriod.SIX_MONTHS) }
                )
                PeriodChip(
                    text = "1年",
                    selected = selectedPeriod == GrowthPeriod.YEAR,
                    onClick = { onPeriodChanged(GrowthPeriod.YEAR) }
                )
            }
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}