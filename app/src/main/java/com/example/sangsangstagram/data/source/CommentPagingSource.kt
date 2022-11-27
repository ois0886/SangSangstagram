package com.example.sangsangstagram.data.source

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.sangsangstagram.data.CommentRepository
import com.example.sangsangstagram.data.model.CommentDto
import com.example.sangsangstagram.data.model.UserDto
import com.example.sangsangstagram.domain.model.Comment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class CommentPagingSource(
    private val getPostUuids: suspend () -> List<String>
) : PagingSource<QuerySnapshot, Comment>() {

    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = Firebase.firestore.collection("users")

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Comment>): QuerySnapshot? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun load(
        params: LoadParams<QuerySnapshot>
    ): LoadResult<QuerySnapshot, Comment> {
        val db = Firebase.firestore
        val commentCollection = db.collection("comments")
        val queryCommentByPost = commentCollection
            .whereIn("postUuid", getPostUuids())
            .orderBy("dataTime", Query.Direction.DESCENDING)
            .limit(CommentRepository.PAGE_SIZE.toLong())

        return try {
            val currentPage = params.key ?: queryCommentByPost.get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
            val lastVisibleComment = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryCommentByPost.startAfter(lastVisibleComment).get().await()
            val commentDtos = currentPage.toObjects(CommentDto::class.java)
            val comments = commentDtos.map { commentDto ->
                val writer = userCollection.document(commentDto.writerUuid).get().await()
                    .toObject(UserDto::class.java)

                Comment(
                    uuid = commentDto.uuid,
                    writerUuid = writer!!.uuid,
                    writerName = writer.name,
                    writerProfileImageUrl = writer.profileImageUrl,
                    content = commentDto.content,
                    isMine = commentDto.writerUuid == currentUserId
                )
            }
            LoadResult.Page(
                data = comments,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}