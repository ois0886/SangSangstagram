package com.example.sangsangstagram.data.model

import java.time.LocalDateTime

data class PostDto(
    val uuid: String = "",
    val writerUuid: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val dataTime: String = ""
)