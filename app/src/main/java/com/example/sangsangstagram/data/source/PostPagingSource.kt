package com.example.sangsangstagram.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.sangsangstagram.data.PostRepository
import com.example.sangsangstagram.data.model.BookMarkDto
import com.example.sangsangstagram.data.model.LikeDto
import com.example.sangsangstagram.data.model.PostDto
import com.example.sangsangstagram.data.model.UserDto
import com.example.sangsangstagram.domain.model.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class PostPagingSource(
    private val getWriterUuids: suspend () -> List<String>
) : PagingSource<QuerySnapshot, Post>() {

    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val likeCollection = Firebase.firestore.collection("likes")
    private val bookMarkCollection = Firebase.firestore.collection("bookmarks")
    private val userCollection = Firebase.firestore.collection("users")
    private val queryPosts =
        Firebase.firestore.collection("posts").orderBy("dateTime", Query.Direction.DESCENDING)
            .limit(PostRepository.PAGE_SIZE.toLong())

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        return null
    }

    override suspend fun load(
        params: LoadParams<QuerySnapshot>
    ): LoadResult<QuerySnapshot, Post> {
        val writerUuidList = getWriterUuids()

        return try {
            val currentPage = params.key ?: queryPosts.get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(
                    data = emptyList(), prevKey = null, nextKey = null
                )
            }
            val lastVisiblePost = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryPosts.startAfter(lastVisiblePost).get().await()
            val postDtos = currentPage.toObjects(PostDto::class.java)
            val posts = postDtos
                .filter { postDto ->
                    writerUuidList.contains(postDto.writerUuid)
                }.map { postDto ->
                    val likes = likeCollection.whereEqualTo("postUuid", postDto.uuid).get().await()
                        .toObjects(LikeDto::class.java)
                    val writer = userCollection.document(postDto.writerUuid).get().await()
                        .toObject(UserDto::class.java)
                    val meLiked = likes.any { like -> like.userUuid == currentUserId }
                    val bookmarks =
                        bookMarkCollection.whereEqualTo("postUuid", postDto.uuid).get().await()
                            .toObjects(BookMarkDto::class.java)
                    val meBookmarked =
                        bookmarks.any { bookmark -> bookmark.userUuid == currentUserId }

                    Post(
                        uuid = postDto.uuid,
                        writerUuid = writer!!.uuid,
                        writerName = writer.name,
                        writerProfileImageUrl = writer.profileImageUrl,
                        content = postDto.content,
                        imageUrl = postDto.imageUrl,
                        likeCount = likes.size,
                        meLiked = meLiked,
                        isMine = postDto.writerUuid == currentUserId,
                        time = postDto.dateTime.toString(),
                        bookMarkChecked = meBookmarked

                    )
                }

            LoadResult.Page(
                data = posts, prevKey = null, nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}