package com.example.hellogithub.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// LanguageUtils.kt
object LanguageUtils {

    private const val PREFS_NAME = "Settings"
    private const val LANGUAGE_KEY = "Selected_Language"

    private fun saveLanguage(language: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(LANGUAGE_KEY, language).apply()
    }


    fun loadLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    fun applyLanguage(language: String, context: Context) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
//        context.window.decorView.post { context.recreate() }
        saveLanguage(language, context)
    }
}
