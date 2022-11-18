package com.example.sangsangstagram.data.model

import java.util.*

data class PostDto(
    val uuid: String = "",
    val writerUuid: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val dataTime: String = ""
)