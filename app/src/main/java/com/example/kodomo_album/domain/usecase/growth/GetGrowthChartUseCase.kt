package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthChartData
import com.example.kodomo_album.domain.model.GrowthType
import com.example.kodomo_album.domain.model.GrowthPeriod
import com.example.kodomo_album.domain.repository.GrowthRepository
import javax.inject.Inject

class GetGrowthChartUseCase @Inject constructor(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(
        childId: String,
        type: GrowthType = GrowthType.ALL,
        period: GrowthPeriod = GrowthPeriod.ALL
    ): Resource<GrowthChartData> {
        return try {
            if (childId.isEmpty()) {
                return Resource.Error("子どもIDが指定されていません")
            }
            
            growthRepository.getGrowthChart(childId, type, period)
        } catch (e: Exception) {
            Resource.Error("成長グラフデータの取得に失敗しました: ${e.message}")
        }
    }
}