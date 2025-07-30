package com.example.kodomoalbum.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.kodomo_album.data.local.dao.*
import com.example.kodomoalbum.data.model.*
import com.example.kodomoalbum.domain.repository.ExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ChildBackupData(
    val id: String,
    val name: String,
    val birthDate: String,
    val gender: String,
    val profileImageUrl: String? = null
)

@Serializable
data class BackupData(
    val exportDate: String,
    val child: ChildBackupData,
    val diaries: List<String> = emptyList(), // 簡素化のため
    val media: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    val milestones: List<String> = emptyList(),
    val growthRecords: List<String> = emptyList()
)

@Singleton
class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val childDao: ChildDao,
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao,
    private val eventDao: EventDao,
    private val milestoneDao: MilestoneDao,
    private val growthRecordDao: GrowthRecordDao
) : ExportRepository {
    
    override suspend fun exportToPdf(request: ExportRequest): Flow<ExportProgress> = flow {
        emit(ExportProgress("データを取得中...", 0.1f))
        
        try {
            val child = childDao.getChildById(request.childId)
                ?: throw Exception("子どもの情報が見つかりません")
            
            emit(ExportProgress("PDFを作成中...", 0.3f))
            
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4サイズ
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas = page.canvas
            val paint = Paint().apply {
                textSize = 24f
                isAntiAlias = true
            }
            
            // タイトルを描画
            canvas.drawText("${child.name}さんの成長記録", 50f, 100f, paint)
            
            emit(ExportProgress("日記データを追加中...", 0.5f))
            
            // 日記データを取得して描画（簡素化）
            val diaries = diaryDao.getDiariesByChildId(request.childId).take(5)
            var yPosition = 150f
            
            paint.textSize = 16f
            diaries.forEach { diary ->
                canvas.drawText("日記: ${diary.title}", 50f, yPosition, paint)
                yPosition += 30f
                canvas.drawText("日付: ${diary.date}", 70f, yPosition, paint)
                yPosition += 50f
            }
            
            emit(ExportProgress("PDFを保存中...", 0.8f))
            
            pdfDocument.finishPage(page)
            
            // ファイルを保存
            val fileName = "growth_record_${child.name}_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
            }
            
            emit(ExportProgress("完了", 1.0f, true))
            
        } catch (e: Exception) {
            emit(ExportProgress("エラー: ${e.message}", 0f))
        }
    }
    
    override suspend fun exportToBackup(request: ExportRequest): Flow<ExportProgress> = flow {
        emit(ExportProgress("データを取得中...", 0.1f))
        
        try {
            val child = childDao.getChildById(request.childId)
                ?: throw Exception("子どもの情報が見つかりません")
            
            emit(ExportProgress("バックアップデータを作成中...", 0.3f))
            
            val backupData = BackupData(
                exportDate = LocalDate.now().toString(),
                child = ChildBackupData(
                    id = child.id,
                    name = child.name,
                    birthDate = child.birthDate.toString(),
                    gender = child.gender,
                    profileImageUrl = child.profileImageUrl
                )
            )
            
            emit(ExportProgress("JSONファイルを作成中...", 0.7f))
            
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(backupData)
            
            // ファイルを保存
            val fileName = "backup_${child.name}_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            withContext(Dispatchers.IO) {
                file.writeText(jsonString)
            }
            
            emit(ExportProgress("完了", 1.0f, true))
            
        } catch (e: Exception) {
            emit(ExportProgress("エラー: ${e.message}", 0f))
        }
    }
    
    override suspend fun getExportResult(requestId: String): ExportResult? {
        // 実装は簡素化
        return null
    }
}