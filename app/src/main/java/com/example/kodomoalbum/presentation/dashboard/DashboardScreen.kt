package com.example.kodomoalbum.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kodomoalbum.data.model.*
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    childId: String,
    onNavigateToSearch: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToDetail: (String, String) -> Unit, // type, id
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    
    LaunchedEffect(childId) {
        viewModel.loadDashboardData(childId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ダッシュボード",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "検索")
                }
                IconButton(onClick = onNavigateToExport) {
                    Icon(Icons.Default.FileDownload, contentDescription = "エクスポート")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refreshData(childId) }
                        ) {
                            Text("再試行")
                        }
                    }
                }
            }
            
            dashboardData != null -> {
                DashboardContent(
                    data = dashboardData!!,
                    onNavigateToDetail = onNavigateToDetail,
                    onRefresh = { viewModel.refreshData(childId) }
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    onNavigateToDetail: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 子ども情報
        item {
            ChildInfoCard(child = data.child)
        }
        
        // 成長サマリー
        item {
            GrowthSummaryCard(
                growthSummary = data.growthSummary,
                onViewDetails = { onNavigateToDetail("growth", data.child.id) }
            )
        }
        
        // 統計情報
        item {
            StatisticsCard(statistics = data.statistics)
        }
        
        // 最近のアクティビティ
        item {
            RecentActivitiesCard(
                activities = data.recentActivities,
                onActivityClick = { activity ->
                    when (activity.type) {
                        ActivityType.DIARY_CREATED -> onNavigateToDetail("diary", activity.id)
                        ActivityType.PHOTO_ADDED -> onNavigateToDetail("media", activity.id)
                        ActivityType.EVENT_RECORDED -> onNavigateToDetail("event", activity.id)
                        ActivityType.MILESTONE_ACHIEVED -> onNavigateToDetail("milestone", activity.id)
                        ActivityType.GROWTH_RECORDED -> onNavigateToDetail("growth", activity.id)
                    }
                }
            )
        }
        
        // 今後のイベント
        if (data.upcomingEvents.isNotEmpty()) {
            item {
                UpcomingEventsCard(
                    events = data.upcomingEvents,
                    onEventClick = { event ->
                        onNavigateToDetail("event", event.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChildInfoCard(child: Child) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Child,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "生年月日: ${child.birthDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GrowthSummaryCard(
    growthSummary: GrowthSummary,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "成長記録",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(onClick = onViewDetails) {
                    Text("詳細を見る")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "現在 ${growthSummary.currentAge}ヶ月",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "身長",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = growthSummary.latestHeight?.let { "${it}cm" } ?: "未記録",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    growthSummary.heightGrowth?.let { growth ->
                        Text(
                            text = if (growth > 0) "+${growth}cm" else "${growth}cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (growth > 0) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "体重",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = growthSummary.latestWeight?.let { "${it}kg" } ?: "未記録",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    growthSummary.weightGrowth?.let { growth ->
                        Text(
                            text = if (growth > 0) "+${growth}kg" else "${growth}kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (growth > 0) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(statistics: Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "統計情報",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem("写真", statistics.totalPhotos, statistics.thisMonthPhotos)
                StatisticItem("日記", statistics.totalDiaries, statistics.thisMonthDiaries)
                StatisticItem("イベント", statistics.totalEvents, 0)
                StatisticItem("発達記録", statistics.totalMilestones, 0)
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    total: Int,
    thisMonth: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$total",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (thisMonth > 0) {
            Text(
                text = "今月+$thisMonth",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun RecentActivitiesCard(
    activities: List<RecentActivity>,
    onActivityClick: (RecentActivity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "最近のアクティビティ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (activities.isEmpty()) {
                Text(
                    text = "まだアクティビティがありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activities.take(5).forEach { activity ->
                    ActivityItem(
                        activity = activity,
                        onClick = { onActivityClick(activity) }
                    )
                    if (activities.indexOf(activity) < activities.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityItem(
    activity: RecentActivity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getActivityIcon(activity.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.date.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventsCard(
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "今後のイベント",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventItem(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = event.eventDate.format(DateTimeFormatter.ofPattern("MM月dd日")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.PHOTO_ADDED -> Icons.Default.Photo
        ActivityType.DIARY_CREATED -> Icons.Default.MenuBook
        ActivityType.MILESTONE_ACHIEVED -> Icons.Default.EmojiEvents
        ActivityType.EVENT_RECORDED -> Icons.Default.Event
        ActivityType.GROWTH_RECORDED -> Icons.Default.TrendingUp
    }
}