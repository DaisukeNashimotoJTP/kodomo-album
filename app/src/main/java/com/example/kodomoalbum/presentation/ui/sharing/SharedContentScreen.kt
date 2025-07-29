package com.example.kodomoalbum.presentation.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomoalbum.domain.model.SharedContent
import com.example.kodomoalbum.domain.model.SharedContentType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedContentScreen(
    onNavigateBack: () -> Unit,
    viewModel: SharedContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadSharedContent()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("共有されたコンテンツ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.sharedContent.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "共有されたコンテンツはありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sharedContent) { content ->
                        SharedContentCard(
                            content = content,
                            onUnshare = { viewModel.unshareContent(content.contentId) },
                            isUnsharing = uiState.unsharingContentId == content.contentId
                        )
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
private fun SharedContentCard(
    content: SharedContent,
    onUnshare: () -> Unit,
    isUnsharing: Boolean
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        getContentTypeIcon(content.contentType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = getContentTypeDisplayName(content.contentType),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "共有日: ${content.createdAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "${content.sharedWith.size}人と共有中",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isUnsharing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = onUnshare) {
                        Text("共有解除")
                    }
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "権限: ${getPermissionDescription(content.permissions)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getContentTypeIcon(contentType: SharedContentType): ImageVector {
    return when (contentType) {
        SharedContentType.PHOTO -> Icons.Default.Image
        SharedContentType.VIDEO -> Icons.Default.MovieCreation
        SharedContentType.DIARY -> Icons.Default.Note
        else -> Icons.Default.Share
    }
}

private fun getContentTypeDisplayName(contentType: SharedContentType): String {
    return when (contentType) {
        SharedContentType.PHOTO -> "写真"
        SharedContentType.VIDEO -> "動画"
        SharedContentType.DIARY -> "日記"
        SharedContentType.GROWTH_RECORD -> "成長記録"
        SharedContentType.MILESTONE -> "発達記録"
        SharedContentType.EVENT -> "イベント"
    }
}

private fun getPermissionDescription(permissions: com.example.kodomoalbum.domain.model.SharedPermissions): String {
    val permissionList = mutableListOf<String>()
    if (permissions.canView) permissionList.add("閲覧")
    if (permissions.canEdit) permissionList.add("編集")
    if (permissions.canDelete) permissionList.add("削除")
    if (permissions.canComment) permissionList.add("コメント")
    
    return if (permissionList.isNotEmpty()) {
        permissionList.joinToString(", ")
    } else {
        "なし"
    }
}