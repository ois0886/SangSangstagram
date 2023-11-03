package com.example.sangsangstagram.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.sangsangstagram.data.UserRepository
import com.example.sangsangstagram.data.model.UserDto
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

class UserPagingSource(
    private val userReferences: List<DocumentReference>
) : PagingSource<Int, UserDto>() {

    override fun getRefreshKey(state: PagingState<Int, UserDto>): Int? {
        return null
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, UserDto> {
        return try {
            val currentPage = params.key ?: 0
            val nextPage = minOf(currentPage + UserRepository.PAGE_SIZE, userReferences.size)
            val userDtoList = userReferences.subList(currentPage, nextPage).map {
                it.get().await().toObject(UserDto::class.java)!!
            }

            LoadResult.Page(
                data = userDtoList,
                prevKey = null,
                nextKey = if (nextPage == userReferences.size) null else nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}