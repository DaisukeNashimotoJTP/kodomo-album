package com.example.kodomoalbum.presentation.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomoalbum.domain.model.Family
import com.example.kodomoalbum.domain.model.FamilyMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToInvitations: () -> Unit,
    onNavigateToSharedContent: () -> Unit,
    viewModel: FamilyManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadFamilies()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家族管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSharedContent) {
                        Icon(Icons.Default.Settings, contentDescription = "共有設定")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToInvitePartner
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "パートナーを招待")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 招待状セクション
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToInvitations
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "招待状",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "受信した招待を確認",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (uiState.pendingInvitationsCount > 0) {
                        Badge {
                            Text(uiState.pendingInvitationsCount.toString())
                        }
                    }
                }
            }
            
            // 家族一覧
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.families.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "家族がありません",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "パートナーを招待して家族を作成しましょう",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.families) { family ->
                            FamilyCard(
                                family = family,
                                onLeaveFamily = { viewModel.leaveFamily(family.id) },
                                isLeaving = uiState.leavingFamilyId == family.id
                            )
                        }
                    }
                }
            }
        }
        
        if (uiState.error != null) {
            LaunchedEffect(uiState.error) {
                // TODO: Show snackbar
            }
        }
    }
}

@Composable
private fun FamilyCard(
    family: Family,
    onLeaveFamily: () -> Unit,
    isLeaving: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = family.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "${family.members.size}人のメンバー",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isLeaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = onLeaveFamily) {
                        Text("退出")
                    }
                }
            }
            
            Divider()
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "メンバー",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                family.members.forEach { member ->
                    FamilyMemberRow(member = member)
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberRow(
    member: FamilyMember
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
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