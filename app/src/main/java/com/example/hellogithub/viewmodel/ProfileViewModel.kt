package com.example.hellogithub.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hellogithub.data.AccessTokenRequest
import com.example.hellogithub.data.GitHubRepo
import com.example.hellogithub.data.GitHubUser
import com.example.hellogithub.data.IssueRequest
import com.example.hellogithub.data.IssueResponse
import com.example.hellogithub.data.Repository
import com.example.hellogithub.utils.MyRetrofit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val dataStore: DataStore<Preferences> // 引入 DataStore 进行安全存储
) : ViewModel() {

    private val _user = MutableLiveData<GitHubUser?>()
    val user: LiveData<GitHubUser?> get() = _user

    private val _repos = MutableLiveData<List<GitHubRepo>>()
    val repos: LiveData<List<GitHubRepo>> get() = _repos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _issueCreationResult = MutableLiveData<IssueResponse?>()
    val issueCreationResult: LiveData<IssueResponse?> get() = _issueCreationResult

    // 定义 Preferences Key
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")

    init {
        _isLoggedIn.value = false
        checkAccessToken()
    }

    private fun checkAccessToken() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val storedToken = preferences[ACCESS_TOKEN_KEY]
                if (!storedToken.isNullOrEmpty()) {
                    login(storedToken)
                }
            }
        }
    }

    fun getAccessToken(clientId: String, clientSecret: String, code: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = MyRetrofit.auth.getAccessToken(
                    AccessTokenRequest(clientId, clientSecret, code)
                )
                val accessToken = response.access_token

                saveAccessToken(accessToken)

                login(accessToken)
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoggedIn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitIssue(repo: Repository, title: String, body: String?) {
        viewModelScope.launch {
            val owner = repo.full_name.split("/")[0]
            val repoName = repo.full_name.split("/")[1]
            try {
                val token = getAccessToken() // 获取 Access Token
                val response = MyRetrofit.api.createIssue(
                    authToken = "Bearer $token",
                    owner = owner,
                    repo = repoName,
                    issueRequest = IssueRequest(title, body)
                )
                _issueCreationResult.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                _issueCreationResult.postValue(null)
            }
        }
    }

    private suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    private suspend fun getAccessToken(): String {
        val preferences = dataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY] ?: ""
    }

    private fun login(accessToken: String) {
        viewModelScope.launch {
            try {
                val userInfo = MyRetrofit.api.getUser("Bearer $accessToken")
                _user.postValue(userInfo)
                _isLoggedIn.value = true

                val userRepos = MyRetrofit.api.getUserRepos("Bearer $accessToken")
                _repos.postValue(userRepos)
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
            }
        }
        _user.postValue(null)
        _repos.postValue(emptyList())
        _isLoggedIn.value = false
    }
}