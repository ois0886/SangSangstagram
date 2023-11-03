package com.example.sangsangstagram.data

import android.graphics.Bitmap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sangsangstagram.data.model.FollowDto
import com.example.sangsangstagram.data.model.UserDto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.example.sangsangstagram.data.source.UserPagingSource
import com.example.sangsangstagram.domain.model.UserDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

object UserRepository {

    const val PAGE_SIZE = 30

    private var followingList: MutableList<FollowDto>? = null
    private var allUserList: MutableList<UserDto>? = null

    suspend fun getAllUserList(): Result<List<UserDto>> {
        if (allUserList != null) {
            return Result.success(requireNotNull(allUserList))
        }
        val userCollection =
            Firebase.firestore.collection("users")
                .orderBy("name")
                .limit(PAGE_SIZE.toLong())
        try {
            val userSnapshot = userCollection.get().await()
            if (userSnapshot.isEmpty) {
                allUserList = mutableListOf()
                return Result.success(requireNotNull(allUserList))
            }
            allUserList = userSnapshot.documents.map {
                requireNotNull(it.toObject(UserDto::class.java))
            }.toMutableList()
            return Result.success(requireNotNull(allUserList))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getFollowingUsersPaging(followerUuid: String): Flow<PagingData<UserDto>> {
        val db = Firebase.firestore
        val userCollection = db.collection("users")
        val followCollection = db.collection("followers")

        try {
            val followeeUuids = followCollection.whereEqualTo("followerUuid", followerUuid)
                .get().await()
                .documents.map {
                    requireNotNull(it.toObject(FollowDto::class.java)).followingUuid
                }
            if (followeeUuids.isEmpty()) {
                return emptyFlow()
            }
            val userReferences = followeeUuids.map { userCollection.document(it) }

            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                UserPagingSource(userReferences = userReferences)
            }.flow
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getFollowersPaging(followeeUuid: String): Flow<PagingData<UserDto>> {
        val db = Firebase.firestore
        val userCollection = db.collection("users")
        val followCollection = db.collection("followers")

        try {
            val followerUuids = followCollection.whereEqualTo("followeeUuid", followeeUuid)
                .get().await()
                .documents.map {
                    requireNotNull(it.toObject(FollowDto::class.java)).followerUuid
                }
            if (followerUuids.isEmpty()) {
                return emptyFlow()
            }
            val userReferences = followerUuids.map { userCollection.document(it) }

            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                UserPagingSource(userReferences = userReferences)
            }.flow
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun saveInitUserInfo(
        name: String,
        profileImage: Bitmap?,
    ): Result<Unit> {
        val user = Firebase.auth.currentUser
        require(user != null)
        val userDto = UserDto(
            uuid = user.uid,
            name = name,
            email = user.email,
            introduce = "안녕하세요."
        )

        val userReference = Firebase.firestore.collection("users").document(user.uid)

        try {
            userReference.set(userDto).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (profileImage == null) {
            return Result.success(Unit)
        }

        val uuid = UUID.randomUUID().toString()
        val imageUrl = "${uuid}.png"
        val imageReference = Firebase.storage.reference.child(imageUrl)
        val byteArrayOutputStream = ByteArrayOutputStream()

        profileImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        try {
            imageReference.putBytes(data).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val newUserDto = userDto.copy(profileImageUrl = imageUrl)

        try {
            userReference.set(newUserDto).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    suspend fun updateInfo(
        name: String,
        introduce: String,
        profileImage: Bitmap?,
        isChangedImage: Boolean,
    ): Result<Unit> {

        val user = Firebase.auth.currentUser
        require(user != null)

        val userMap = mutableMapOf<String, Any>(
            "name" to name,
            "introduce" to introduce,
        )

        val userReference = Firebase.firestore.collection("users").document(user.uid)

        if (isChangedImage && profileImage == null) {
            userMap["profileImageUrl"] = FieldValue.delete()
        } else if (isChangedImage && profileImage != null) {
            val uuid = UUID.randomUUID().toString()
            val imageUrl = "${uuid}.png"
            val imageReference = Firebase.storage.reference.child(imageUrl)
            val byteArrayOutputStream = ByteArrayOutputStream()

            profileImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            try {
                imageReference.putBytes(data).await()
            } catch (e: Exception) {
                return Result.failure(e)
            }

            userMap["profileImageUrl"] = imageUrl
        }

        try {
            userReference.update(userMap.toMap()).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    suspend fun follow(targetUserUuid: String): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        val followCollection = Firebase.firestore.collection("followers")
        check(currentUser != null)

        try {
            val alreadyFollowing = !followCollection
                .whereEqualTo("followerUuid", currentUser.uid)
                .whereEqualTo("followingUuid", targetUserUuid)
                .get().await().isEmpty
            if (alreadyFollowing) {
                throw IllegalStateException("이미 팔로우 중인 상대를 팔로우 하였습니다.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val followDto = FollowDto(
            uuid = UUID.randomUUID().toString(),
            followingUuid = targetUserUuid,
            followerUuid = currentUser.uid
        )
        return try {
            followCollection.document(followDto.uuid).set(followDto).await()
            followingList?.add(followDto)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun unfollow(targetUserUuid: String): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        val followCollection = Firebase.firestore.collection("followers")
        check(currentUser != null)

        return try {
            val followDto = followCollection
                .whereEqualTo("followerUuid", currentUser.uid)
                .whereEqualTo("followingUuid", targetUserUuid)
                .get().await().first().toObject(FollowDto::class.java)

            followCollection.document(followDto.uuid).delete()
            followingList?.remove(followDto)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getUserDetail(userUuid: String): Result<UserDetail> {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val userCollection = db.collection("users")
        val postCollection = db.collection("posts")
        val followCollection = db.collection("followers")
        check(currentUser != null)

        try {
            val userDto = userCollection.document(userUuid)
                .get().await().toObject(UserDto::class.java)!!
            val postCount = postCollection.whereEqualTo("writerUuid", userUuid).count()
                .get(AggregateSource.SERVER).await().count
            val followersCount = followCollection.whereEqualTo("followingUuid", userUuid)
                .count().get(AggregateSource.SERVER).await().count
            val followingCount = followCollection.whereEqualTo("followerUuid", userUuid)
                .count().get(AggregateSource.SERVER).await().count
            val isCurrentUserFollowing = !followCollection
                .whereEqualTo("followerUuid", currentUser.uid)
                .whereEqualTo("followingUuid", userUuid)
                .get().await().isEmpty

            return Result.success(
                UserDetail(
                    uuid = userDto.uuid,
                    name = userDto.name,
                    email = userDto.email,
                    introduce = userDto.introduce,
                    profileImageUrl = userDto.profileImageUrl,
                    postCount = postCount,
                    followersCount = followersCount,
                    followingCount = followingCount,
                    isCurrentUserFollowing = isCurrentUserFollowing,
                    isMe = userUuid == currentUser.uid
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}
