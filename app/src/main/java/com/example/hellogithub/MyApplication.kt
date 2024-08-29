package com.example.hellogithub

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.example.hellogithub.utils.LanguageUtils
import com.example.hellogithub.utils.ThemeUtils

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        // load the language when app start
        val savedLanguage = LanguageUtils.loadLanguage(this)
        LanguageUtils.applyLanguage(savedLanguage, this)
    }
}