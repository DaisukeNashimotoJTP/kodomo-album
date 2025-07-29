package com.example.kodomo_album.presentation.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.presentation.media.MediaTimelineViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    diaryId: String,
    onNavigateUp: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToMediaDetail: (String) -> Unit,
    onShareClick: (() -> Unit)? = null,
    diaryViewModel: DiaryDetailViewModel = hiltViewModel(),
    mediaViewModel: MediaTimelineViewModel = hiltViewModel()
) {
    val diaryState by diaryViewModel.uiState.collectAsStateWithLifecycle()
    val mediaState by mediaViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(diaryId) {
        diaryViewModel.loadDiary(diaryId)
    }

    LaunchedEffect(diaryState.diary) {
        diaryState.diary?.let { diary ->
            if (diary.mediaIds.isNotEmpty()) {
                mediaViewModel.loadMediaByIds(diary.mediaIds)
            }
        }
    }

    LaunchedEffect(Unit) {
        diaryViewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = diaryState.diary?.title ?: "日記",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    if (diaryState.diary != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(diaryId) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "編集")
                        }
                        
                        onShareClick?.let { shareAction ->
                            IconButton(onClick = shareAction) {
                                Icon(Icons.Default.Share, contentDescription = "共有")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            diaryState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            diaryState.diary == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("日記が見つかりません")
                }
            }
            
            else -> {
                DiaryDetailContent(
                    diary = diaryState.diary!!,
                    associatedMedia = mediaState.mediaList.filter { media ->
                        diaryState.diary!!.mediaIds.contains(media.id)
                    },
                    onMediaClick = onNavigateToMediaDetail,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DiaryDetailContent(
    diary: Diary,
    associatedMedia: List<Media>,
    onMediaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "日付",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = diary.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Content
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "内容",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = diary.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Associated Media
        if (associatedMedia.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "写真・動画 (${associatedMedia.size}件)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(associatedMedia) { media ->
                            MediaThumbnail(
                                media = media,
                                onClick = { onMediaClick(media.id) }
                            )
                        }
                    }
                }
            }
        }

        // Metadata
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "作成日時",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = diary.createdAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (diary.createdAt != diary.updatedAt) {
                    Text(
                        text = "更新日時",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = diary.updatedAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaThumbnail(
    media: Media,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.size(80.dp),
        onClick = onClick
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(media.thumbnailUrl ?: media.url)
                .crossfade(true)
                .build(),
            contentDescription = media.caption,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}