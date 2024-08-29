package com.example.hellogithub.utils

import android.content.Context
import com.example.hellogithub.MyApplication
import com.example.hellogithub.api.AuthApi
import com.example.hellogithub.api.GitHubApi
import com.example.hellogithub.interceptor.MyInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MyRetrofit {
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(MyInterceptor(context)) // Add the custom interceptor
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(getOkHttpClient(MyApplication.context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .client(getOkHttpClient(MyApplication.context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GitHubApi by lazy {
        retrofit.create(GitHubApi::class.java)
    }

    val auth: AuthApi by lazy {
        authRetrofit.create(AuthApi::class.java)
    }
}