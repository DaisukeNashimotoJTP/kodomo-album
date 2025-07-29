package com.example.kodomo_album.presentation.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kodomo_album.domain.model.GrowthDataPoint
import com.example.kodomo_album.domain.model.GrowthType
import com.example.kodomo_album.domain.model.GrowthPeriod
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthChartScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: GrowthChartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(childId) {
        viewModel.loadGrowthChart(childId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // トップバー
        TopAppBar(
            title = { Text("成長グラフ") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // フィルター選択
            GrowthFilterSection(
                selectedType = state.selectedType,
                selectedPeriod = state.selectedPeriod,
                onTypeChanged = viewModel::setGrowthType,
                onPeriodChanged = viewModel::setGrowthPeriod
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
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // グラフ表示
                val chartData = state.chartData
                if (chartData != null) {
                    if (state.selectedType == GrowthType.HEIGHT || state.selectedType == GrowthType.ALL) {
                        GrowthChart(
                            title = "身長の変化",
                            dataPoints = chartData.heightData,
                            unit = "cm",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (state.selectedType == GrowthType.WEIGHT || state.selectedType == GrowthType.ALL) {
                        GrowthChart(
                            title = "体重の変化",
                            dataPoints = chartData.weightData,
                            unit = "kg",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (state.selectedType == GrowthType.HEAD_CIRCUMFERENCE || state.selectedType == GrowthType.ALL) {
                        GrowthChart(
                            title = "頭囲の変化",
                            dataPoints = chartData.headCircumferenceData,
                            unit = "cm",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // データがない場合
                if (chartData == null || 
                    (chartData.heightData.isEmpty() && 
                     chartData.weightData.isEmpty() && 
                     chartData.headCircumferenceData.isEmpty())) {
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
                                text = "グラフデータがありません",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "成長記録を追加してグラフを表示しましょう",
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
private fun GrowthChart(
    title: String,
    dataPoints: List<GrowthDataPoint>,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawGrowthChart(dataPoints, color, unit)
            }

            // 最新の値表示
            dataPoints.lastOrNull()?.let { lastPoint ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "最新値: ${lastPoint.value}$unit (${lastPoint.date})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun DrawScope.drawGrowthChart(
    dataPoints: List<GrowthDataPoint>,
    color: Color,
    unit: String
) {
    if (dataPoints.size < 2) return

    val padding = 40f
    val chartWidth = size.width - padding * 2
    val chartHeight = size.height - padding * 2

    // データの範囲を計算
    val minValue = dataPoints.minOf { it.value }
    val maxValue = dataPoints.maxOf { it.value }
    val valueRange = maxValue - minValue
    val adjustedMinValue = minValue - valueRange * 0.1
    val adjustedMaxValue = maxValue + valueRange * 0.1
    val adjustedRange = adjustedMaxValue - adjustedMinValue

    // 日付の範囲を計算
    val minDate = dataPoints.minOf { it.date.toEpochDay() }
    val maxDate = dataPoints.maxOf { it.date.toEpochDay() }
    val dateRange = maxDate - minDate

    // グリッドラインを描画
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    
    // 縦のグリッドライン（5本）
    for (i in 0..4) {
        val x = padding + (chartWidth * i / 4)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    // 横のグリッドライン（5本）
    for (i in 0..4) {
        val y = padding + (chartHeight * i / 4)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // データポイントを描画用座標に変換
    val points = dataPoints.map { point ->
        val x = if (dateRange > 0) {
            padding + chartWidth * (point.date.toEpochDay() - minDate).toFloat() / dateRange
        } else {
            padding + chartWidth / 2
        }
        
        val y = if (adjustedRange > 0) {
            padding + chartHeight - (chartHeight * (point.value - adjustedMinValue) / adjustedRange).toFloat()
        } else {
            padding + chartHeight / 2
        }
        
        Offset(x, y)
    }

    // 線を描画
    if (points.size > 1) {
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }

    // データポイントを描画
    points.forEach { point ->
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = point
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = point
        )
    }
}