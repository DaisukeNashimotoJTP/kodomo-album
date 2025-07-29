package com.example.kodomoalbum.presentation.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kodomoalbum.domain.model.FamilyMember

@Composable
fun ShareContentDialog(
    onDismiss: () -> Unit,
    onShare: (List<String>) -> Unit,
    familyMembers: List<FamilyMember>,
    isLoading: Boolean = false
) {
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text(
                        text = "家族と共有",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                Text(
                    text = "共有する家族メンバーを選択してください",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (familyMembers.isEmpty()) {
                    Text(
                        text = "共有可能な家族メンバーがいません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(familyMembers) { member ->
                            FamilyMemberCheckboxItem(
                                member = member,
                                isSelected = selectedMembers.contains(member.userId),
                                onSelectedChange = { isSelected ->
                                    selectedMembers = if (isSelected) {
                                        selectedMembers + member.userId
                                    } else {
                                        selectedMembers - member.userId
                                    }
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("キャンセル")
                    }
                    
                    Button(
                        onClick = { onShare(selectedMembers.toList()) },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && selectedMembers.isNotEmpty()
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("共有中...")
                            }
                        } else {
                            Text("共有")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberCheckboxItem(
    member: FamilyMember,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelectedChange(!isSelected) },
                role = Role.Checkbox
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectedChange
        )
        
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = member.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (member.role.name == "ADMIN") {
            AssistChip(
                onClick = { },
                label = { Text("管理者") },
                enabled = false
            )
        }
    }
}