package com.example.kodomo_album.presentation.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterDialog(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStartDate by remember { mutableStateOf(startDate) }
    var selectedEndDate by remember { mutableStateOf(endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("期間で絞り込み") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 開始日選択
                DatePickerField(
                    label = "開始日",
                    selectedDate = selectedStartDate,
                    onDateClick = { showStartDatePicker = true },
                    onClearClick = { selectedStartDate = null }
                )

                // 終了日選択
                DatePickerField(
                    label = "終了日",
                    selectedDate = selectedEndDate,
                    onDateClick = { showEndDatePicker = true },
                    onClearClick = { selectedEndDate = null }
                )

                // 期間プリセット
                Text(
                    text = "よく使う期間",
                    style = MaterialTheme.typography.titleSmall
                )

                DatePresetButtons(
                    onPresetSelected = { startDate, endDate ->
                        selectedStartDate = startDate
                        selectedEndDate = endDate
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeChanged(selectedStartDate, selectedEndDate)
                    onDismiss()
                }
            ) {
                Text("適用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )

    // 開始日DatePicker
    if (showStartDatePicker) {
        DatePickerModal(
            initialDate = selectedStartDate ?: LocalDate.now(),
            onDateSelected = { date ->
                selectedStartDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // 終了日DatePicker
    if (showEndDatePicker) {
        DatePickerModal(
            initialDate = selectedEndDate ?: LocalDate.now(),
            onDateSelected = { date ->
                selectedEndDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun DatePickerField(
    label: String,
    selectedDate: LocalDate?,
    onDateClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) 
                        ?: "日付を選択"
                )
            }

            if (selectedDate != null) {
                TextButton(onClick = onClearClick) {
                    Text("クリア")
                }
            }
        }
    }
}

@Composable
private fun DatePresetButtons(
    onPresetSelected: (LocalDate, LocalDate) -> Unit
) {
    val today = LocalDate.now()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    onPresetSelected(today.minusDays(7), today)
                },
                label = { Text("過去1週間") },
                modifier = Modifier.weight(1f)
            )
            
            AssistChip(
                onClick = {
                    onPresetSelected(today.minusMonths(1), today)
                },
                label = { Text("過去1ヶ月") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    onPresetSelected(today.minusMonths(3), today)
                },
                label = { Text("過去3ヶ月") },
                modifier = Modifier.weight(1f)
            )
            
            AssistChip(
                onClick = {
                    onPresetSelected(today.minusYears(1), today)
                },
                label = { Text("過去1年") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("選択")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}