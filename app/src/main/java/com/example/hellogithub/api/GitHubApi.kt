package com.example.hellogithub.api

import com.example.hellogithub.data.AccessTokenRequest
import com.example.hellogithub.data.AccessTokenResponse
import com.example.hellogithub.data.GitHubRepo
import com.example.hellogithub.data.GitHubUser
import com.example.hellogithub.data.IssueRequest
import com.example.hellogithub.data.IssueResponse
import com.example.hellogithub.data.Repository
import com.example.hellogithub.data.SearchResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface GitHubApi {
    // Fetch user information
    @GET("user")
    suspend fun getUser(@Header("Authorization") authToken: String): GitHubUser

    // Fetch user's repositories
    @GET("user/repos")
    suspend fun getUserRepos(@Header("Authorization") authToken: String): List<GitHubRepo>

    @GET("repositories")
    suspend fun getAllRepositories(): List<Repository>

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String? = null
    ): SearchResult

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Header("Authorization") authToken: String,
        @Path("owner") owner: String,               // 仓库拥有者
        @Path("repo") repo: String,                 // 仓库名称
        @Body issueRequest: IssueRequest            // Issue 请求体
    ): IssueResponse
}