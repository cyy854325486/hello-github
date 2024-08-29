package com.example.hellogithub.interceptor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.hellogithub.R
import okhttp3.Interceptor
import okhttp3.Response

class MyInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            val response = chain.proceed(request)
            // Only handle failure cases
            if (!response.isSuccessful) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, context.getString(R.string.request_error), Toast.LENGTH_SHORT).show()
                }
            }
            response
        } catch (e: Exception) {
            // Show dialog on request failure
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, context.getString(R.string.request_error), Toast.LENGTH_SHORT).show()
            }
            throw e
        }
    }
}
