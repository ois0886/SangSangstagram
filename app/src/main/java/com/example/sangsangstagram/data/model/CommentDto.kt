package com.example.sangsangstagram.data.model

data class CommentDto(
    val uuid: String = "",
    val postUuid: String = "",
    val writerUuid: String = "",
    val content: String = ""
)