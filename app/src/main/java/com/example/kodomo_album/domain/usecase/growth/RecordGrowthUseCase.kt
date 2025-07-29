package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.repository.GrowthRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RecordGrowthUseCase @Inject constructor(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(growthRecord: GrowthRecord): Resource<GrowthRecord> {
        return try {
            // バリデーション
            val validationError = validateGrowthRecord(growthRecord)
            if (validationError != null) {
                return Resource.Error(validationError)
            }
            
            // IDが空の場合は新しいIDを生成
            val recordWithId = if (growthRecord.id.isEmpty()) {
                growthRecord.copy(id = UUID.randomUUID().toString())
            } else {
                growthRecord
            }
            
            growthRepository.recordGrowth(recordWithId)
        } catch (e: Exception) {
            Resource.Error("成長記録の保存に失敗しました: ${e.message}")
        }
    }
    
    private fun validateGrowthRecord(record: GrowthRecord): String? {
        // 子どもIDチェック
        if (record.childId.isEmpty()) {
            return "子どもIDが指定されていません"
        }
        
        // 少なくとも1つの測定値が必要
        if (record.height == null && record.weight == null && record.headCircumference == null) {
            return "身長、体重、頭囲のうち少なくとも1つは入力してください"
        }
        
        // 数値バリデーション
        record.height?.let { height ->
            if (height < 0) return "身長は0以上である必要があります"
            if (height > 200) return "身長は200cm以下である必要があります"
        }
        
        record.weight?.let { weight ->
            if (weight < 0) return "体重は0以上である必要があります"
            if (weight > 100) return "体重は100kg以下である必要があります"
        }
        
        record.headCircumference?.let { headCircumference ->
            if (headCircumference < 0) return "頭囲は0以上である必要があります"
            if (headCircumference > 100) return "頭囲は100cm以下である必要があります"
        }
        
        // 記録日チェック
        if (record.recordedAt.isAfter(LocalDate.now())) {
            return "記録日は現在日付以前である必要があります"
        }
        
        return null
    }
}