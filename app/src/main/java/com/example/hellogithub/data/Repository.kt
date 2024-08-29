package com.example.hellogithub.data

data class Repository (
    val id: Long,
    val name: String,
    val description: String?,
    val owner: Owner,
    val html_url: String,
    val full_name: String
)