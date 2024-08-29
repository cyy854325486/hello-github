package com.example.hellogithub.data

data class IssueResponse(
    val id: Int,
    val number: Int,
    val title: String,
    val body: String?
)