package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.repository.GrowthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetGrowthHistoryUseCase @Inject constructor(
    private val growthRepository: GrowthRepository
) {
    operator fun invoke(childId: String): Flow<Resource<List<GrowthRecord>>> = flow {
        if (childId.isEmpty()) {
            emit(Resource.Error("子どもIDが指定されていません"))
            return@flow
        }
        
        try {
            growthRepository.getGrowthHistory(childId).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Resource.Error("成長記録の取得に失敗しました: ${e.message}"))
        }
    }
}