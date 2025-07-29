package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.repository.GrowthRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateGrowthRecordUseCase @Inject constructor(
    private val growthRepository: GrowthRepository,
    private val recordGrowthUseCase: RecordGrowthUseCase
) {
    suspend operator fun invoke(growthRecord: GrowthRecord): Resource<GrowthRecord> {
        return try {
            if (growthRecord.id.isEmpty()) {
                return Resource.Error("更新するレコードのIDが指定されていません")
            }
            
            // 既存レコードの存在確認
            when (val existingRecord = growthRepository.getGrowthRecordById(growthRecord.id)) {
                is Resource.Error -> return Resource.Error("既存レコードの取得に失敗しました: ${existingRecord.message}")
                is Resource.Loading -> return Resource.Error("データ確認中です")
                is Resource.Success -> {
                    if (existingRecord.data == null) {
                        return Resource.Error("更新対象のレコードが見つかりません")
                    }
                }
            }
            
            // バリデーションは RecordGrowthUseCase と同じロジックを使用
            val validationResult = recordGrowthUseCase(growthRecord)
            if (validationResult is Resource.Error) {
                return validationResult
            }
            
            growthRepository.updateGrowthRecord(growthRecord)
        } catch (e: Exception) {
            Resource.Error("成長記録の更新に失敗しました: ${e.message}")
        }
    }
}