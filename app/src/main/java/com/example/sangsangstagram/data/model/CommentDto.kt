package com.example.sangsangstagram.data.model

import java.util.*

data class CommentDto(
    val uuid: String = "",
    val postUuid: String = "",
    val writerUuid: String = "",
    val content: String = "",
    val dateTime: Date = Date()
)