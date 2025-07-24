package com.example.kodomo_album.core.util

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthException -> getAuthErrorMessage(throwable)
            is FirebaseFirestoreException -> getFirestoreErrorMessage(throwable)
            is FirebaseNetworkException -> Constants.NETWORK_ERROR_MESSAGE
            is UnknownHostException, is SocketTimeoutException -> Constants.NETWORK_ERROR_MESSAGE
            is IOException -> Constants.NETWORK_ERROR_MESSAGE
            is FirebaseException -> throwable.message ?: Constants.GENERIC_ERROR_MESSAGE
            else -> throwable.message ?: Constants.GENERIC_ERROR_MESSAGE
        }
    }
    
    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "無効なメールアドレスです"
            "ERROR_WRONG_PASSWORD" -> "パスワードが正しくありません"
            "ERROR_USER_NOT_FOUND" -> "ユーザーが見つかりません"
            "ERROR_USER_DISABLED" -> "アカウントが無効化されています"
            "ERROR_TOO_MANY_REQUESTS" -> "リクエストが多すぎます。しばらく時間をおいてから再試行してください"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "このメールアドレスは既に使用されています"
            "ERROR_WEAK_PASSWORD" -> "パスワードが弱すぎます"
            "ERROR_NETWORK_REQUEST_FAILED" -> Constants.NETWORK_ERROR_MESSAGE
            else -> exception.message ?: Constants.AUTH_ERROR_MESSAGE
        }
    }
    
    private fun getFirestoreErrorMessage(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "アクセス権限がありません"
            FirebaseFirestoreException.Code.NOT_FOUND -> "データが見つかりません"
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "既に存在します"
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "リクエスト制限に達しました"
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> "前提条件が満たされていません"
            FirebaseFirestoreException.Code.ABORTED -> "処理が中断されました"
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> "範囲外の値です"
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> "実装されていない機能です"
            FirebaseFirestoreException.Code.INTERNAL -> "内部エラーが発生しました"
            FirebaseFirestoreException.Code.UNAVAILABLE -> "サービスが利用できません"
            FirebaseFirestoreException.Code.DATA_LOSS -> "データが破損しています"
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "認証が必要です"
            else -> exception.message ?: Constants.GENERIC_ERROR_MESSAGE
        }
    }
}