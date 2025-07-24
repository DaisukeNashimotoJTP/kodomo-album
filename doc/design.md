# 子どもの成長記録アプリ 設計書

## 概要
子どもの成長記録に必要な機能（写真・動画、日記、身長体重、発達記録、イベント記録、家族共有）を統合したAndroidアプリケーション。MVVM+Repositoryパターンを採用し、Clean Architectureの原則に従って設計。データはRoom（ローカル）とFirebase（クラウド）で管理し、リアルタイム同期と家族共有を実現する。

## アーキテクチャ
### システム構成
```
[Android App] <-> [Firebase Auth] <-> [Firebase Firestore]
      |                                       |
      v                                       v
[Room Database] <-> [Local Storage] <-> [Cloud Storage]
```

### レイヤー構造
1. **Presentation Layer (UI)**
   - Activity/Fragment
   - ViewModel
   - Compose UI Components

2. **Domain Layer (Business Logic)**
   - Use Cases
   - Repository Interfaces
   - Entity Models

3. **Data Layer**
   - Repository Implementations
   - Data Sources (Local/Remote)
   - Room Database
   - Firebase Integration

## コンポーネントとインタフェース
### 主要コンポーネント

#### 1. 認証システム (AuthComponent)
```kotlin
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun signOut()
}
```

#### 2. メディア管理システム (MediaComponent)
```kotlin
interface MediaRepository {
    suspend fun uploadPhoto(file: File, metadata: MediaMetadata): Result<Media>
    suspend fun uploadVideo(file: File, metadata: MediaMetadata): Result<Media>
    suspend fun getMediaList(childId: String, dateRange: DateRange?): List<Media>
    suspend fun deleteMedia(mediaId: String): Result<Unit>
}
```

#### 3. 日記システム (DiaryComponent)
```kotlin
interface DiaryRepository {
    suspend fun createDiary(diary: Diary): Result<Diary>
    suspend fun getDiaries(childId: String, dateRange: DateRange?): List<Diary>
    suspend fun updateDiary(diary: Diary): Result<Diary>
    suspend fun searchDiaries(keyword: String): List<Diary>
}
```

#### 4. 成長記録システム (GrowthComponent)
```kotlin
interface GrowthRepository {
    suspend fun recordGrowth(growth: GrowthRecord): Result<GrowthRecord>
    suspend fun getGrowthHistory(childId: String): List<GrowthRecord>
    suspend fun getGrowthChart(childId: String, type: GrowthType): ChartData
}
```

#### 5. 発達記録システム (DevelopmentComponent)
```kotlin
interface DevelopmentRepository {
    suspend fun recordMilestone(milestone: Milestone): Result<Milestone>
    suspend fun getMilestones(childId: String): List<Milestone>
    suspend fun getStandardMilestones(): List<StandardMilestone>
}
```

#### 6. 家族共有システム (SharingComponent)
```kotlin
interface SharingRepository {
    suspend fun invitePartner(email: String): Result<Invitation>
    suspend fun acceptInvitation(invitationId: String): Result<Unit>
    suspend fun shareContent(contentId: String, targetUsers: List<String>): Result<Unit>
    suspend fun getSharedContent(): List<SharedContent>
}
```

## データモデル

### Core Entities
```kotlin
data class Child(
    val id: String,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val profileImageUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Media(
    val id: String,
    val childId: String,
    val type: MediaType, // PHOTO, VIDEO, ECHO
    val url: String,
    val thumbnailUrl: String?,
    val caption: String?,
    val takenAt: LocalDateTime,
    val uploadedAt: LocalDateTime
)

data class Diary(
    val id: String,
    val childId: String,
    val title: String,
    val content: String,
    val mediaIds: List<String>,
    val date: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class GrowthRecord(
    val id: String,
    val childId: String,
    val height: Double?, // cm
    val weight: Double?, // kg
    val headCircumference: Double?, // cm
    val recordedAt: LocalDate,
    val notes: String?
)

data class Milestone(
    val id: String,
    val childId: String,
    val type: MilestoneType, // MOTOR, LANGUAGE, SOCIAL, COGNITIVE
    val title: String,
    val description: String,
    val achievedAt: LocalDate,
    val mediaIds: List<String>
)

data class Event(
    val id: String,
    val childId: String,
    val title: String,
    val description: String,
    val eventDate: LocalDate,
    val mediaIds: List<String>,
    val eventType: EventType // BIRTHDAY, FIRST_STEP, CEREMONY
)
```

### Database Schema (Room)
```kotlin
@Entity(tableName = "children")
data class ChildEntity(...)

@Entity(tableName = "media")
data class MediaEntity(...)

@Entity(tableName = "diaries")
data class DiaryEntity(...)

@Entity(tableName = "growth_records")
data class GrowthRecordEntity(...)

@Entity(tableName = "milestones")
data class MilestoneEntity(...)

@Entity(tableName = "events")
data class EventEntity(...)
```

## エラーハンドリング

### エラータイプ定義
```kotlin
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object AuthenticationError : AppError()
    object StorageError : AppError()
    object ValidationError : AppError()
    data class ServerError(val code: Int, val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}
```

### エラーハンドリング戦略
1. **ネットワークエラー**: 自動リトライ機能（指数バックオフ）
2. **認証エラー**: ログイン画面への遷移
3. **ストレージエラー**: ローカル保存への切り替え
4. **バリデーションエラー**: ユーザーフレンドリーなメッセージ表示

## テスト戦略

1. **ユニットテスト**
   - ViewModelのロジックテスト (JUnit + MockK)
   - Repository実装のテスト
   - Use Caseのビジネスロジックテスト
   - カバレッジ目標: 80%以上

2. **統合テスト**
   - Room DatabaseのCRUD操作テスト
   - Firebase統合テスト（エミュレータ使用）
   - API通信テスト（MockWebServer使用）

3. **E2Eテスト**
   - 主要ユーザーフローのテスト（Espresso）
   - 写真アップロード〜表示の一連の流れ
   - 日記作成〜共有の一連の流れ
   - 成長記録入力〜グラフ表示の一連の流れ
   - 家族招待〜コンテンツ共有の一連の流れ

## 技術スタック

### 開発環境
- **IDE**: Android Studio
- **言語**: Kotlin
- **ビルドシステム**: Gradle (KTS)

### フレームワーク・ライブラリ
- **UI**: Jetpack Compose
- **アーキテクチャ**: MVVM + Repository Pattern
- **DI**: Dagger Hilt
- **ローカルDB**: Room
- **ネットワーク**: Retrofit + OkHttp
- **画像処理**: Glide / Coil
- **非同期処理**: Kotlin Coroutines + Flow

### バックエンド・インフラ
- **認証**: Firebase Authentication
- **データベース**: Firebase Firestore
- **ストレージ**: Firebase Cloud Storage
- **プッシュ通知**: Firebase Cloud Messaging
- **分析**: Firebase Analytics
- **クラッシュレポート**: Firebase Crashlytics

### テスト
- **単体テスト**: JUnit 5, MockK, Truth
- **統合テスト**: Room Testing, Firebase Test Lab
- **E2Eテスト**: Espresso, UI Automator

## パフォーマンス考慮事項

### 画像・動画処理
- **画像圧縮**: アップロード前に適切なサイズ・品質に圧縮
- **遅延読み込み**: RecyclerView/LazyColumnでの画像遅延読み込み
- **キャッシュ戦略**: Glideによる3段階キャッシュ（メモリ・ディスク・ネットワーク）

### データベース最適化
- **Room**: 適切なインデックス設定、プリコンパイルされたクエリ
- **Firestore**: 複合インデックス、効率的なクエリ設計
- **オフライン対応**: Room + Firestoreでのオフライン・オンライン同期

### ネットワーク最適化
- **バッチ処理**: 複数のデータ更新を一括送信
- **差分同期**: 変更されたデータのみ同期
- **接続状態監視**: ネットワーク状態に応じた処理切り替え

### メモリ管理
- **画像メモリリーク対策**: WeakReferenceの活用
- **ViewModelスコープ管理**: ライフサイクルを考慮したリスナー管理
- **バックグラウンド処理制限**: Android 12+の制限に対応

## セキュリティ設計

### データ暗号化
- **通信**: TLS 1.3による通信暗号化
- **ローカルストレージ**: Room database暗号化（SQLCipher）
- **共有設定**: EncryptedSharedPreferencesの使用

### 認証・認可
- **多要素認証**: Firebase AuthのMFA対応
- **トークン管理**: JWT refresh tokenによる自動更新
- **権限制御**: 家族間での細かな権限設定

### プライバシー保護
- **データ最小化**: 必要最小限のデータのみ収集
- **匿名化**: 分析データの匿名化処理
- **GDPR対応**: データ削除・エクスポート機能の実装