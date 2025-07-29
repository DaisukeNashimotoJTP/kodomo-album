package com.example.kodomo_album.presentation.child

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kodomo_album.R
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Child
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildListScreen(
    userId: String,
    onAddChildClick: () -> Unit,
    onEditChildClick: (Child) -> Unit,
    onAddMediaClick: () -> Unit = {},
    onViewDiaryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChildManagementViewModel = hiltViewModel()
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf<Child?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadChildren(userId)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    // Handle snackbar
                }
                is UiEvent.Navigate -> {
                    // Handle navigation
                }
                UiEvent.NavigateUp -> {
                    // Handle navigate up
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("子ども管理") }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddMediaClick,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "写真・動画を追加")
                }
                FloatingActionButton(
                    onClick = onAddChildClick
                ) {
                    Icon(Icons.Default.Add, contentDescription = "子どもを追加")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (children.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "まだお子さまが登録されていません",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "最初のお子さまを追加して、成長と思い出の記録を始めましょう",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddChildClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("子どもを追加")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(children) { child ->
                        ChildItem(
                            child = child,
                            onEditClick = { onEditChildClick(child) },
                            onDeleteClick = { showDeleteDialog = child },
                            onViewDiaryClick = { onViewDiaryClick(child.id) }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { child ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("子どもの削除") },
            text = { Text("${child.name}を削除しますか？この操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChild(child.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun ChildItem(
    child: Child,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewDiaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = child.profileImageUrl,
                contentDescription = "${child.name}'s photo",
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "生年月日: ${child.birthDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (child.gender) {
                        com.example.kodomo_album.domain.model.Gender.MALE -> "男の子"
                        com.example.kodomo_album.domain.model.Gender.FEMALE -> "女の子"
                        com.example.kodomo_album.domain.model.Gender.OTHER -> "その他"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onViewDiaryClick) {
                    Icon(Icons.Default.Book, contentDescription = "${child.name}の日記")
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "${child.name}を編集")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "${child.name}を削除")
                }
            }
        }
    }
}