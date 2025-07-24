package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

abstract class BaseRepository {
    
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return try {
            Resource.Success(apiCall())
        } catch (e: HttpException) {
            Resource.Error(
                message = e.localizedMessage ?: "予期しないエラーが発生しました"
            )
        } catch (e: IOException) {
            Resource.Error(
                message = "ネットワーク接続を確認してください"
            )
        } catch (e: Exception) {
            Resource.Error(
                message = e.localizedMessage ?: "予期しないエラーが発生しました"
            )
        }
    }
    
    protected fun <T> safeApiFlow(
        apiCall: suspend () -> T
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading())
        
        try {
            val result = apiCall()
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "予期しないエラーが発生しました"
                )
            )
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "ネットワーク接続を確認してください"
                )
            )
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "予期しないエラーが発生しました"
                )
            )
        }
    }
}