package com.example.hellogithub.utils

import android.app.Activity
import android.content.Context
import com.example.hellogithub.R

object ThemeUtils {
    private const val PREFS_NAME = "settings"
    private const val THEME_KEY = "theme"

    fun loadTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(THEME_KEY, "light") ?: "light"
    }

    fun applySavedTheme(activity: Activity) {
        val theme = loadTheme(activity)
        val themeResId = if (theme == "dark") R.style.Theme_MyApp_Dark else R.style.Theme_MyApp_Light
        activity.setTheme(themeResId)
    }

    fun applyTheme(theme: String, activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(THEME_KEY, theme).apply()

        // Apply the theme and restart the activity
        val themeResId = if (theme == "dark") R.style.Theme_MyApp_Dark else R.style.Theme_MyApp_Light
        activity.setTheme(themeResId)
        activity.recreate()
    }
}

