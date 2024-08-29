package com.example.hellogithub.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hellogithub.MainActivity
import com.example.hellogithub.utils.LanguageUtils
import com.example.hellogithub.utils.ThemeUtils
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to track current language
    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language

    // LiveData to track current theme
    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> get() = _theme

    init {
        _language.value = LanguageUtils.loadLanguage(application)
        _theme.value = ThemeUtils.loadTheme(application)
    }

    // Function to apply the selected language
    fun applyLanguage(language: String, activity: Activity) {
        _language.value = language
        LanguageUtils.applyLanguage(language, activity)
        activity.recreate()
    }

    fun applyTheme(theme: String, activity: Activity) {
        _theme.value = theme
        ThemeUtils.applyTheme(theme, activity) // Assume you have this utility
        activity.recreate()
    }
}
