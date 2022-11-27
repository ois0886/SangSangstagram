package com.example.sangsangstagram.data

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sangsangstagram.data.model.PostDto
import com.example.sangsangstagram.data.source.CommentPagingSource
import com.example.sangsangstagram.domain.model.Comment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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
                CommentPagingSource(getPostUuids = {
                    val result = UserRepository.getAllUserList()
                    if (result.isSuccess) {
                        result.getOrNull()!!.map { it.uuid }.toMutableList()
                    } else {
                        throw IllegalStateException("회원 정보 얻기 실패")
                    }
                })
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadComment(
        content: String,
        imageUri: Uri
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val postCollection = db.collection("posts")
        val imageFileName: String = UUID.randomUUID().toString() + ".png"
        val imageRef = storageRef.child(imageFileName)
        val postUuid = UUID.randomUUID().toString()

        try {
            imageRef.putFile(imageUri).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val postDto = PostDto(
                uuid = postUuid,
                writerUuid = currentUser.uid,
                content = content,
                imageUrl = imageFileName,
                dateTime = Date()
            )
            postCollection.document(postUuid).set(postDto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(postUuid: String): Result<Unit> {
        val db = Firebase.firestore

        return try {
            db.collection("posts").document(postUuid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editComment(
        uuid: String,
        content: String,
        imageUri: Uri
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val postCollection = db.collection("posts")
        val imageFileName: String = UUID.randomUUID().toString() + ".png"
        val imageRef = storageRef.child(imageFileName)
        val map = mutableMapOf<String, Any>()

        map["content"] = content

        try {
            imageRef.putFile(imageUri).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            postCollection.document(uuid).update(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
