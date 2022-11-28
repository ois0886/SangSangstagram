package com.example.sangsangstagram.domain.model

data class Comment(
    val uuid: String,
    val writerUuid: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val content: String,
    val isMine: Boolean,
    val time: String
)