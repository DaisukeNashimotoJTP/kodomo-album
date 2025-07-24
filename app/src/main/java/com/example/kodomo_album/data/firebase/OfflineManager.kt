package com.example.kodomo_album.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncService: SyncService
) {
    companion object {
        private const val TAG = "OfflineManager"
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _networkState = MutableStateFlow(NetworkState.UNKNOWN)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _syncQueue = MutableStateFlow<List<SyncItem>>(emptyList())
    val syncQueue: StateFlow<List<SyncItem>> = _syncQueue.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var queueProcessingJob: Job? = null

    init {
        startNetworkMonitoring()
        startQueueProcessing()
    }

    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                _networkState.value = NetworkState.CONNECTED
                processQueueWhenOnline()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                _networkState.value = NetworkState.DISCONNECTED
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                if (hasInternet && validated) {
                    _networkState.value = NetworkState.CONNECTED
                    processQueueWhenOnline()
                } else {
                    _networkState.value = NetworkState.DISCONNECTED
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
        
        // 初期状態を確認
        checkInitialNetworkState()
    }

    private fun checkInitialNetworkState() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        _networkState.value = if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                                  networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            NetworkState.CONNECTED
        } else {
            NetworkState.DISCONNECTED
        }
    }

    private fun startQueueProcessing() {
        queueProcessingJob = networkScope.launch {
            networkState
                .filter { it == NetworkState.CONNECTED }
                .collect {
                    processOfflineQueue()
                }
        }
    }

    // オフライン時のデータをキューに追加
    fun queueForSync(item: SyncItem) {
        val currentQueue = _syncQueue.value.toMutableList()
        
        // 同じアイテムが既にキューにある場合は更新
        val existingIndex = currentQueue.indexOfFirst { 
            it.id == item.id && it.type == item.type 
        }
        
        if (existingIndex >= 0) {
            currentQueue[existingIndex] = item
        } else {
            currentQueue.add(item)
        }
        
        _syncQueue.value = currentQueue
        Log.d(TAG, "Item queued for sync: ${item.type} - ${item.id}")
    }

    // キューからアイテムを削除
    private fun removeFromQueue(item: SyncItem) {
        val currentQueue = _syncQueue.value.toMutableList()
        currentQueue.removeAll { it.id == item.id && it.type == item.type }
        _syncQueue.value = currentQueue
    }

    // オンライン復帰時のキュー処理
    private fun processQueueWhenOnline() {
        if (_networkState.value == NetworkState.CONNECTED) {
            networkScope.launch {
                processOfflineQueue()
            }
        }
    }

    // オフラインキューの処理
    private suspend fun processOfflineQueue() {
        if (_networkState.value != NetworkState.CONNECTED) {
            Log.d(TAG, "Network not available, skipping queue processing")
            return
        }

        val queue = _syncQueue.value
        if (queue.isEmpty()) {
            Log.d(TAG, "Sync queue is empty")
            return
        }

        Log.d(TAG, "Processing ${queue.size} items in sync queue")

        for (item in queue) {
            try {
                val result = when (item.type) {
                    SyncItemType.DIARY -> syncService.syncSpecificDiary(item.id)
                    SyncItemType.MEDIA -> syncService.syncSpecificMedia(item.id)
                    SyncItemType.CHILD -> Result.success(Unit) // 子ども情報は通常すぐ同期される
                }

                if (result.isSuccess) {
                    removeFromQueue(item)
                    Log.d(TAG, "Successfully synced: ${item.type} - ${item.id}")
                } else {
                    Log.w(TAG, "Failed to sync: ${item.type} - ${item.id}", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing queue item: ${item.type} - ${item.id}", e)
            }
            
            // 各アイテムの処理間に少し待機
            delay(100)
        }
    }

    // 手動でキューを処理
    suspend fun processQueueManually(): Result<Unit> {
        return try {
            processOfflineQueue()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process queue manually", e)
            Result.failure(e)
        }
    }

    // ネットワーク状態の確認
    fun isNetworkAvailable(): Boolean {
        return _networkState.value == NetworkState.CONNECTED
    }

    // オフラインモードでの作業継続可能性の確認
    fun canWorkOffline(): Boolean {
        // ローカルデータベースがあるため、オフラインでも基本的な機能は使用可能
        return true
    }

    // キューの統計情報
    fun getQueueStats(): QueueStats {
        val queue = _syncQueue.value
        return QueueStats(
            totalItems = queue.size,
            diaryItems = queue.count { it.type == SyncItemType.DIARY },
            mediaItems = queue.count { it.type == SyncItemType.MEDIA },
            childItems = queue.count { it.type == SyncItemType.CHILD }
        )
    }

    // リソースクリーンアップ
    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager.unregisterNetworkCallback(callback)
        }
        queueProcessingJob?.cancel()
        networkScope.cancel()
    }
}

enum class NetworkState {
    UNKNOWN,
    CONNECTED,
    DISCONNECTED
}

enum class SyncItemType {
    DIARY,
    MEDIA,
    CHILD
}

data class SyncItem(
    val id: String,
    val type: SyncItemType,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val data: Map<String, Any> = emptyMap()
)

data class QueueStats(
    val totalItems: Int,
    val diaryItems: Int,
    val mediaItems: Int,
    val childItems: Int
)