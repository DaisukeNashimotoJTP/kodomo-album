package com.example.kodomo_album.presentation.media

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    media: Media,
    onNavigateUp: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null
) {
    var showFullScreen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // トップバー
        TopAppBar(
            title = { 
                Text(
                    text = when (media.type) {
                        MediaType.VIDEO -> "動画詳細"
                        MediaType.ECHO -> "エコー写真詳細"
                        MediaType.PHOTO -> "写真詳細"
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                }
            },
            actions = {
                // 編集ボタン
                onEditClick?.let { editAction ->
                    IconButton(onClick = editAction) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                }
                
                // 削除ボタン
                onDeleteClick?.let { deleteAction ->
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // 共有ボタン
                onShareClick?.let { shareAction ->
                    IconButton(onClick = shareAction) {
                        Icon(Icons.Default.Share, contentDescription = "共有")
                    }
                }
            }
        )

        // メディア表示エリア
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (media.type == MediaType.VIDEO) {
                    // 動画の場合
                    VideoThumbnailWithPlayButton(
                        media = media,
                        onPlayClick = { /* TODO: 動画再生 */ },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 画像の場合
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = media.caption ?: "メディア",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // フルスクリーン表示ボタン
                    IconButton(
                        onClick = { showFullScreen = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "フルスクリーン",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // メディア情報
        MediaInfoSection(
            media = media,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }

    // フルスクリーン表示ダイアログ
    if (showFullScreen) {
        FullScreenImageDialog(
            media = media,
            onDismiss = { showFullScreen = false }
        )
    }

    // 削除確認ダイアログ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("削除の確認") },
            text = { Text("この${if (media.type == MediaType.VIDEO) "動画" else "写真"}を削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick?.invoke()
                    }
                ) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun VideoThumbnailWithPlayButton(
    media: Media,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // サムネイル
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(media.thumbnailUrl ?: media.url)
                .crossfade(true)
                .build(),
            contentDescription = media.caption ?: "動画サムネイル",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        
        // 再生ボタン
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(40.dp)
                )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "再生",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun MediaInfoSection(
    media: Media,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // メディアタイプと日時
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (media.type) {
                            MediaType.VIDEO -> "動画"
                            MediaType.ECHO -> "エコー写真"
                            MediaType.PHOTO -> "写真"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = media.takenAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 撮影時刻
            Text(
                text = "撮影時刻: ${media.takenAt.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // キャプション
            media.caption?.let { caption ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "メモ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // アップロード日時
            Text(
                text = "追加日時: ${media.uploadedAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FullScreenImageDialog(
    media: Media,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // ズーム可能な画像
            ZoomableImage(
                media = media,
                modifier = Modifier.fillMaxSize()
            )
            
            // 閉じるボタン
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "閉じる",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    media: Media,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset = offset + offsetChange
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(media.url)
            .crossfade(true)
            .build(),
        contentDescription = media.caption ?: "画像",
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = transformableState),
        contentScale = ContentScale.Fit
    )
}