package com.example.kodomo_album.presentation.growth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kodomo_album.domain.model.GrowthSummary
import java.time.format.DateTimeFormatter

@Composable
fun GrowthSummaryCard(
    summary: GrowthSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "成長サマリー (${summary.period})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary.recordPeriod.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 最新の測定値
            Text(
                text = "最新の測定値",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                summary.latestHeight?.let { height ->
                    MeasurementValueCard(
                        title = "身長",
                        value = "${height}cm",
                        growth = summary.heightGrowth,
                        unit = "cm",
                        modifier = Modifier.weight(1f)
                    )
                }

                summary.latestWeight?.let { weight ->
                    MeasurementValueCard(
                        title = "体重",
                        value = "${weight}kg",
                        growth = summary.weightGrowth,
                        unit = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }

                summary.latestHeadCircumference?.let { headCircumference ->
                    MeasurementValueCard(
                        title = "頭囲",
                        value = "${headCircumference}cm",
                        growth = null, // 頭囲の成長は計算していない
                        unit = "cm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 統計情報
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "記録統計",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "記録回数: ${summary.totalRecords}回",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementValueCard(
    title: String,
    value: String,
    growth: Double?,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 成長表示
            growth?.let { growthValue ->
                Spacer(modifier = Modifier.height(4.dp))
                GrowthIndicator(
                    growth = growthValue,
                    unit = unit
                )
            }
        }
    }
}

@Composable
private fun GrowthIndicator(
    growth: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when {
        growth > 0.1 -> Triple(
            Icons.Default.TrendingUp,
            MaterialTheme.colorScheme.tertiary,
            "+${String.format("%.1f", growth)}$unit"
        )
        growth < -0.1 -> Triple(
            Icons.Default.TrendingDown,
            MaterialTheme.colorScheme.error,
            "${String.format("%.1f", growth)}$unit"
        )
        else -> Triple(
            Icons.Default.TrendingFlat,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "変化なし"
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}