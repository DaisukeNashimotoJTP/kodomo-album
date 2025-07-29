package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.repository.GrowthRepository
import javax.inject.Inject

class DeleteGrowthRecordUseCase @Inject constructor(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(recordId: String): Resource<Unit> {
        return try {
            if (recordId.isEmpty()) {
                return Resource.Error("削除するレコードのIDが指定されていません")
            }
            
            // 既存レコードの存在確認
            when (val existingRecord = growthRepository.getGrowthRecordById(recordId)) {
                is Resource.Error -> return Resource.Error("削除対象レコードの確認に失敗しました: ${existingRecord.message}")
                is Resource.Loading -> return Resource.Error("データ確認中です")
                is Resource.Success -> {
                    if (existingRecord.data == null) {
                        return Resource.Error("削除対象のレコードが見つかりません")
                    }
                }
            }
            
            growthRepository.deleteGrowthRecord(recordId)
        } catch (e: Exception) {
            Resource.Error("成長記録の削除に失敗しました: ${e.message}")
        }
    }
}