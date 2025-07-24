package com.example.kodomo_album.presentation.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
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
import coil.compose.rememberAsyncImagePainter
import com.example.kodomo_album.core.util.CameraUtils
import com.example.kodomo_album.domain.model.Child
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaUploadScreen(
    children: List<Child>,
    onNavigateBack: () -> Unit,
    viewModel: MediaUploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showMediaPicker by remember { mutableStateOf(false) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.onImageSelected(tempCameraUri!!)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Handle upload success
    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            onNavigateBack()
            viewModel.resetUploadState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("写真・動画を追加") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Child selection
            if (children.isNotEmpty()) {
                Text("子どもを選択", style = MaterialTheme.typography.titleMedium)
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = children.find { it.id == uiState.selectedChildId }?.name ?: "選択してください",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        children.forEach { child ->
                            DropdownMenuItem(
                                text = { Text(child.name) },
                                onClick = {
                                    viewModel.onChildSelected(child.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Media selection buttons
            Text("写真・動画を選択", style = MaterialTheme.typography.titleMedium)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val file = CameraUtils.createImageFile(context)
                        val uri = CameraUtils.getImageUri(context, file)
                        tempCameraFile = file
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("カメラ")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ギャラリー")
                }
            }

            // Selected image preview
            uiState.selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "選択された画像",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Caption input
                OutlinedTextField(
                    value = uiState.caption,
                    onValueChange = viewModel::onCaptionChanged,
                    label = { Text("キャプション（任意）") },
                    placeholder = { Text("写真の説明を入力してください") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Upload button
                Button(
                    onClick = {
                        val file = if (tempCameraUri == uri) {
                            tempCameraFile
                        } else {
                            // For gallery images, we need to copy to a temporary file
                            // This is a simplified version - in production, implement proper file handling
                            File(context.cacheDir, "temp_image.jpg")
                        }
                        file?.let { viewModel.uploadMedia(it) }
                    },
                    enabled = !uiState.isLoading && uiState.selectedChildId.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("アップロード中...")
                    } else {
                        Text("アップロード", fontWeight = FontWeight.Medium)
                    }
                }

                // Clear selection button
                TextButton(
                    onClick = viewModel::clearSelectedMedia,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("選択をクリア")
                }
            }

            if (uiState.selectedImageUri == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "写真・動画を選択してください",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}