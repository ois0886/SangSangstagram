package com.example.sangsangstagram.model

data class UserDTO(
    val uuid: String = "",
    val name: String = "",
    val introduce: String = "",
    val email: String? = null,
    val password: String? = null,
    val profileImageUrl: String? = null
)