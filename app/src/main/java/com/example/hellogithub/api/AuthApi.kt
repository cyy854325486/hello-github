package com.example.hellogithub.api

import com.example.hellogithub.data.AccessTokenRequest
import com.example.hellogithub.data.AccessTokenResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    // Fetch access token using authorization code
    @Headers("Accept: application/json")
    @POST("login/oauth/access_token")
    suspend fun getAccessToken(@Body request: AccessTokenRequest): AccessTokenResponse
}