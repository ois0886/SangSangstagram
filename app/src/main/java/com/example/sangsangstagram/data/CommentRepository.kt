package com.example.sangsangstagram.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sangsangstagram.data.model.CommentDto
import com.example.sangsangstagram.data.source.CommentPagingSource
import com.example.sangsangstagram.domain.model.Comment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

object CommentRepository {

    const val PAGE_SIZE = 20

    suspend fun getComment(postUuid: String): Flow<PagingData<Comment>> {
        try {
            val currentUser = Firebase.auth.currentUser
            require(currentUser != null)

            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                CommentPagingSource(getWriterUuids = {
                    val result = UserRepository.getAllUserList()
                    if (result.isSuccess) {
                        result.getOrNull()!!.map { it.uuid }.toMutableList()
                    } else {
                        throw IllegalStateException("회원 정보 얻기 실패")
                    }
                }, { listOf(postUuid) })
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadComment(
        content: String,
        postUuid: String
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val commentCollection = db.collection("comments")
        val commentsUuid = UUID.randomUUID().toString()

        return try {
            val commentDto = CommentDto(
                uuid = commentsUuid,
                postUuid = postUuid,
                writerUuid = currentUser.uid,
                content = content,
                dateTime = Date()
            )
            commentCollection.document(commentsUuid).set(commentDto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentsUuid: String): Result<Unit> {
        val db = Firebase.firestore

        return try {
            db.collection("comments").document(commentsUuid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editComment(
        uuid: String,
        content: String
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val commentCollection = db.collection("comments")
        val map = mutableMapOf<String, Any>()

        map["content"] = content

        return try {
            commentCollection.document(uuid).update(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
