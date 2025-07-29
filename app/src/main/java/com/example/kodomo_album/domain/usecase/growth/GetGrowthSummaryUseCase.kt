package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthSummary
import com.example.kodomo_album.domain.model.GrowthPeriod
import com.example.kodomo_album.domain.repository.GrowthRepository
import javax.inject.Inject

class GetGrowthSummaryUseCase @Inject constructor(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(
        childId: String,
        period: GrowthPeriod = GrowthPeriod.MONTH
    ): Resource<GrowthSummary> {
        return try {
            if (childId.isEmpty()) {
                return Resource.Error("子どもIDが指定されていません")
            }
            
            growthRepository.getGrowthSummary(childId, period)
        } catch (e: Exception) {
            Resource.Error("成長サマリーの取得に失敗しました: ${e.message}")
        }
    }
}