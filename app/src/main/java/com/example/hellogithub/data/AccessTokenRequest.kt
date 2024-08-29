package com.example.hellogithub.data

data class AccessTokenRequest(
    val client_id: String,
    val client_secret: String,
    val code: String
)
