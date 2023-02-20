package com.example.sangsangstagram.data

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sangsangstagram.data.model.BookMarkDto
import com.example.sangsangstagram.data.model.LikeDto
import com.example.sangsangstagram.data.model.PostDto
import com.example.sangsangstagram.data.source.BookMarkPagingSource
import com.example.sangsangstagram.data.source.PostPagingSource
import com.example.sangsangstagram.domain.model.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

object PostRepository {

    const val PAGE_SIZE = 20

    private suspend fun getBookMarkList(): Result<List<BookMarkDto>> {
        var bookMarkList: MutableList<BookMarkDto>? = null

        if (bookMarkList != null) {
            return Result.success(bookMarkList)
        }
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val bookMarkCollection = db.collection("bookmarks")
        val bookMarkQuery = bookMarkCollection.whereEqualTo("userUuid", currentUser.uid)

        try {
            val bookMarkSnapshot = bookMarkQuery.get().await()
            if (bookMarkSnapshot.isEmpty) {
                bookMarkList = mutableListOf()
                return Result.success(bookMarkList)
            }
            bookMarkList = bookMarkSnapshot.documents.map {
                requireNotNull(it.toObject(BookMarkDto::class.java))
            }.toMutableList()
            return Result.success(bookMarkList)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getBookMarkFeeds(): Flow<PagingData<Post>> {
        try {
            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                BookMarkPagingSource(getPostUuids = {
                    val result = getBookMarkList()
                    if (result.isSuccess) {
                        result.getOrNull()!!.map { it.postUuid }.toMutableList()
                    } else {
                        throw IllegalStateException("북마크 불러 오기 실패")
                    }
                })
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    suspend fun getHomeFeeds(): Flow<PagingData<Post>> {
        try {
            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                PostPagingSource(getWriterUuids = {
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

    fun getPostDetailsByUser(uuid: String): Flow<PagingData<Post>> {
        try {
            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                PostPagingSource(getWriterUuids = { listOf(uuid) })
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun toggleBookMark(postUuid: String): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val bookMarkCollection = db.collection("bookmarks")

        return try {
            val bookMarks = bookMarkCollection
                .whereEqualTo("userUuid", currentUser.uid)
                .whereEqualTo("postUuid", postUuid)
                .get().await().toObjects<BookMarkDto>()

            if (bookMarks.isEmpty()) {
                val bookMarkUuid = UUID.randomUUID().toString()
                val bookMarkDto = BookMarkDto(
                    uuid = bookMarkUuid,
                    userUuid = currentUser.uid,
                    postUuid = postUuid
                )
                bookMarkCollection.document(bookMarkUuid).set(bookMarkDto).await()
            } else {
                bookMarkCollection.document(bookMarks.first().uuid).delete().await()
            }
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun toggleLike(postUuid: String): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val likeCollection = db.collection("likes")

        return try {
            val likes = likeCollection
                .whereEqualTo("userUuid", currentUser.uid)
                .whereEqualTo("postUuid", postUuid)
                .get().await().toObjects<LikeDto>()

            if (likes.isEmpty()) {
                val likeUuid = UUID.randomUUID().toString()
                val likeDto = LikeDto(
                    uuid = likeUuid,
                    userUuid = currentUser.uid,
                    postUuid = postUuid
                )
                likeCollection.document(likeUuid).set(likeDto).await()
            } else {
                likeCollection.document(likes.first().uuid).delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postUuid: String): Result<Unit> {
        val db = Firebase.firestore

        return try {
            db.collection("posts").document(postUuid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editPost(
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
        map["imageUrl"] = imageFileName

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

    suspend fun editPostOnlyContent(
        uuid: String,
        content: String,
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val postCollection = db.collection("posts")
        val map = mutableMapOf<String, Any>()

        map["content"] = content

        return try {
            postCollection.document(uuid).update(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadPost(
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
}