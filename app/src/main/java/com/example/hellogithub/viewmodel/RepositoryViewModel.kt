package com.example.hellogithub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.hellogithub.data.Repository
import com.example.hellogithub.utils.MyRetrofit
import kotlinx.coroutines.launch

class RepositoryViewModel() : ViewModel() {

    // LiveData to hold the list of repositories
    private val _repos = MutableLiveData<List<Repository>>()
    val repos: LiveData<List<Repository>> get() = _repos

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        // Fetch all repositories when the ViewModel is created
        fetchAllRepositories()
    }

    // Function to fetch all repositories
    private fun fetchAllRepositories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val repositories = MyRetrofit.api.getAllRepositories()
                _repos.postValue(repositories)
            } catch (e: Exception) {
                // Handle the exception, possibly by updating a LiveData for errors
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Function to search repositories based on a query
    fun searchRepositories(query: String, language: String, stars: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Construct the query with filters
                val languageFilter = if (language.isNotEmpty()) "language:$language" else ""
                val starsFilter = if (stars > 0) "stars:>=$stars" else ""
                val combinedQuery = listOf(query, languageFilter, starsFilter).filter { it.isNotEmpty() }.joinToString(" ")

                val searchResult = MyRetrofit.api.searchRepositories(combinedQuery)
                _repos.postValue(searchResult.items)
            } catch (e: Exception) {
                // Handle the exception
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}