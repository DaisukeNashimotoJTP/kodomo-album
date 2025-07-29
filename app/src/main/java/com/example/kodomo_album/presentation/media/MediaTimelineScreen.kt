package com.example.kodomo_album.presentation.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomoalbum.presentation.ui.sharing.ShareContentDialog
import com.example.kodomoalbum.presentation.ui.sharing.FamilyManagementViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTimelineScreen(
    childId: String,
    onMediaClick: (Media) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: MediaTimelineViewModel = hiltViewModel(),
    familyViewModel: FamilyManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val familyState by familyViewModel.uiState.collectAsStateWithLifecycle()
    var isGridView by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedMediaForShare: Media? by remember { mutableStateOf(null) }

    LaunchedEffect(childId) {
        viewModel.onChildChanged(childId)
    }
    
    LaunchedEffect(Unit) {
        familyViewModel.loadFamilies()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // トップバー
        TopAppBar(
            title = { Text("思い出タイムライン") },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                }
            },
            actions = {
                // 表示形式切り替え
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        if (isGridView) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = if (isGridView) "リスト表示" else "グリッド表示"
                    )
                }
                
                // フィルター
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "フィルター")
                }
                
                // 日付フィルター
                IconButton(onClick = { showDateFilterDialog = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "日付フィルター")
                }
                
                // 更新
                IconButton(onClick = { viewModel.refreshMediaList() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "更新")
                }
            }
        )

        // エラー表示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // ローディング表示
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // メディア一覧
            if (uiState.mediaList.isEmpty()) {
                // 空の状態
                EmptyMediaState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                if (isGridView) {
                    MediaGridView(
                        mediaList = uiState.mediaList,
                        onMediaClick = onMediaClick,
                        onShareClick = { media ->
                            selectedMediaForShare = media
                            showShareDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MediaListView(
                        mediaList = uiState.mediaList,
                        onMediaClick = onMediaClick,
                        onShareClick = { media ->
                            selectedMediaForShare = media
                            showShareDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // フィルターダイアログ
    if (showFilterDialog) {
        MediaFilterDialog(
            currentMediaType = uiState.selectedMediaType,
            onMediaTypeChanged = { viewModel.onMediaTypeFilterChanged(it) },
            onDismiss = { showFilterDialog = false }
        )
    }

    // 日付フィルターダイアログ
    if (showDateFilterDialog) {
        DateFilterDialog(
            startDate = uiState.dateRange?.first,
            endDate = uiState.dateRange?.second,
            onDateRangeChanged = { startDate, endDate ->
                viewModel.onDateFilterChanged(startDate, endDate)
            },
            onDismiss = { showDateFilterDialog = false }
        )
    }
    
    // 共有ダイアログ
    if (showShareDialog && selectedMediaForShare != null) {
        ShareContentDialog(
            onDismiss = { 
                showShareDialog = false
                selectedMediaForShare = null
            },
            onShare = { selectedUsers ->
                selectedMediaForShare?.let { media ->
                    // TODO: ShareContentUseCaseを呼び出してコンテンツを共有
                }
                showShareDialog = false
                selectedMediaForShare = null
            },
            familyMembers = familyState.families.flatMap { it.members }
        )
    }
}

@Composable
private fun MediaGridView(
    mediaList: List<Media>,
    onMediaClick: (Media) -> Unit,
    onShareClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = modifier
    ) {
        items(mediaList) { media ->
            MediaGridItem(
                media = media,
                onClick = { onMediaClick(media) },
                onShareClick = { onShareClick(media) }
            )
        }
    }
}

@Composable
private fun MediaListView(
    mediaList: List<Media>,
    onMediaClick: (Media) -> Unit,
    onShareClick: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(mediaList) { media ->
            MediaListItem(
                media = media,
                onClick = { onMediaClick(media) },
                onShareClick = { onShareClick(media) }
            )
        }
    }
}

@Composable
private fun MediaGridItem(
    media: Media,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.thumbnailUrl ?: media.url)
                    .crossfade(true)
                    .build(),
                contentDescription = media.caption ?: "メディア",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // メディアタイプアイコン
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = when (media.type) {
                        MediaType.VIDEO -> Icons.Default.PlayArrow
                        MediaType.ECHO -> Icons.Default.Favorite
                        MediaType.PHOTO -> Icons.Default.PhotoCamera
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // 日付表示
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = media.takenAt.format(DateTimeFormatter.ofPattern("MM/dd")),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            // 共有ボタン
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(20.dp)
                    )
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "共有",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MediaListItem(
    media: Media,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // サムネイル
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.thumbnailUrl ?: media.url)
                    .crossfade(true)
                    .build(),
                contentDescription = media.caption ?: "メディア",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 詳細情報
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (media.type) {
                            MediaType.VIDEO -> Icons.Default.PlayArrow
                            MediaType.ECHO -> Icons.Default.Favorite
                            MediaType.PHOTO -> Icons.Default.PhotoCamera
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (media.type) {
                            MediaType.VIDEO -> "動画"
                            MediaType.ECHO -> "エコー写真"
                            MediaType.PHOTO -> "写真"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = media.takenAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                media.caption?.let { caption ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 共有ボタン
            IconButton(onClick = onShareClick) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "共有",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyMediaState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "まだ写真や動画がありません",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "思い出の瞬間を記録してみましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun MediaFilterDialog(
    currentMediaType: MediaType?,
    onMediaTypeChanged: (MediaType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("フィルター") },
        text = {
            Column {
                FilterOption(
                    text = "すべて",
                    isSelected = currentMediaType == null,
                    onClick = { onMediaTypeChanged(null) }
                )
                
                FilterOption(
                    text = "写真",
                    isSelected = currentMediaType == MediaType.PHOTO,
                    onClick = { onMediaTypeChanged(MediaType.PHOTO) }
                )
                
                FilterOption(
                    text = "動画",
                    isSelected = currentMediaType == MediaType.VIDEO,
                    onClick = { onMediaTypeChanged(MediaType.VIDEO) }
                )
                
                FilterOption(
                    text = "エコー写真",
                    isSelected = currentMediaType == MediaType.ECHO,
                    onClick = { onMediaTypeChanged(MediaType.ECHO) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(text = text)
    }
}