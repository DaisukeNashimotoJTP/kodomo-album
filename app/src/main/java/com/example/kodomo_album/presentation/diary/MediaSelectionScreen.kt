package com.example.kodomo_album.presentation.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.presentation.media.MediaTimelineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSelectionScreen(
    childId: String,
    selectedMediaIds: List<String>,
    onMediaSelected: (List<String>) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: MediaTimelineViewModel = hiltViewModel()
) {
    val mediaState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var selectedIds by remember { mutableStateOf(selectedMediaIds.toSet()) }

    LaunchedEffect(childId) {
        viewModel.loadMediaList(childId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "写真・動画を選択",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onMediaSelected(selectedIds.toList())
                            onNavigateUp()
                        }
                    ) {
                        Text("完了 (${selectedIds.size})")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            mediaState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            mediaState.mediaList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "写真・動画がありません",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "まず写真や動画をアップロードしてください",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(mediaState.mediaList) { media ->
                        MediaSelectionItem(
                            media = media,
                            isSelected = selectedIds.contains(media.id),
                            onSelectionChanged = { isSelected ->
                                selectedIds = if (isSelected) {
                                    selectedIds + media.id
                                } else {
                                    selectedIds - media.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaSelectionItem(
    media: Media,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onClick = { onSelectionChanged(!isSelected) }
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(media.thumbnailUrl ?: media.url)
                    .crossfade(true)
                    .build(),
                contentDescription = media.caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Selection overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        modifier = Modifier.padding(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "選択済み",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}