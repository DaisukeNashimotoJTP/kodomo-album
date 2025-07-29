package com.example.kodomo_album.presentation.growth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kodomo_album.domain.model.GrowthType
import com.example.kodomo_album.domain.model.GrowthPeriod

@Composable
fun GrowthFilterSection(
    selectedType: GrowthType,
    selectedPeriod: GrowthPeriod,
    onTypeChanged: (GrowthType) -> Unit,
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
                text = "表示設定",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 成長タイプ選択
            Text(
                text = "表示項目",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(bottom = 16.dp)
            ) {
                GrowthTypeOption(
                    text = "すべて",
                    selected = selectedType == GrowthType.ALL,
                    onClick = { onTypeChanged(GrowthType.ALL) }
                )
                GrowthTypeOption(
                    text = "身長",
                    selected = selectedType == GrowthType.HEIGHT,
                    onClick = { onTypeChanged(GrowthType.HEIGHT) }
                )
                GrowthTypeOption(
                    text = "体重",
                    selected = selectedType == GrowthType.WEIGHT,
                    onClick = { onTypeChanged(GrowthType.WEIGHT) }
                )
                GrowthTypeOption(
                    text = "頭囲",
                    selected = selectedType == GrowthType.HEAD_CIRCUMFERENCE,
                    onClick = { onTypeChanged(GrowthType.HEAD_CIRCUMFERENCE) }
                )
            }

            // 期間選択
            Text(
                text = "期間",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.selectableGroup()
            ) {
                GrowthPeriodOption(
                    text = "すべて",
                    selected = selectedPeriod == GrowthPeriod.ALL,
                    onClick = { onPeriodChanged(GrowthPeriod.ALL) }
                )
                GrowthPeriodOption(
                    text = "1週間",
                    selected = selectedPeriod == GrowthPeriod.WEEK,
                    onClick = { onPeriodChanged(GrowthPeriod.WEEK) }
                )
                GrowthPeriodOption(
                    text = "1ヶ月",
                    selected = selectedPeriod == GrowthPeriod.MONTH,
                    onClick = { onPeriodChanged(GrowthPeriod.MONTH) }
                )
                GrowthPeriodOption(
                    text = "3ヶ月",
                    selected = selectedPeriod == GrowthPeriod.THREE_MONTHS,
                    onClick = { onPeriodChanged(GrowthPeriod.THREE_MONTHS) }
                )
                GrowthPeriodOption(
                    text = "6ヶ月",
                    selected = selectedPeriod == GrowthPeriod.SIX_MONTHS,
                    onClick = { onPeriodChanged(GrowthPeriod.SIX_MONTHS) }
                )
                GrowthPeriodOption(
                    text = "1年",
                    selected = selectedPeriod == GrowthPeriod.YEAR,
                    onClick = { onPeriodChanged(GrowthPeriod.YEAR) }
                )
            }
        }
    }
}

@Composable
private fun GrowthTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun GrowthPeriodOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}