package com.example.watchmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchmobile.data.network.IdlixScraper
import com.example.watchmobile.domain.models.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val categories = listOf("Semua", "Aksi", "Drama", "Animasi", "Horor", "Sci-Fi", "Romantis", "Kriminal", "Misteri", "Series")

    private var currentPage = 1
    private var hasMoreList = true
    private var searchJob: Job? = null

    init {
        fetchMovies()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            refreshMovies()
        }
    }

    fun onCategorySelected(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            refreshMovies()
        }
    }

    fun refreshMovies() {
        currentPage = 1
        hasMoreList = true
        _movies.value = emptyList()
        fetchMovies()
    }

    fun loadMoreMovies() {
        if (_isLoadingMore.value || _isLoading.value || !hasMoreList) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            try {
                val scrapedMovies = IdlixScraper.scrapeCategoryMovies(
                    category = _selectedCategory.value,
                    page = currentPage,
                    searchQuery = _searchQuery.value
                )

                if (scrapedMovies.isEmpty()) {
                    hasMoreList = false
                } else {
                    _movies.value = _movies.value + scrapedMovies
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val scrapedMovies = IdlixScraper.scrapeCategoryMovies(
                    category = _selectedCategory.value,
                    page = currentPage,
                    searchQuery = _searchQuery.value
                )

                _movies.value = scrapedMovies
                if (scrapedMovies.isEmpty()) {
                    hasMoreList = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "Terjadi kesalahan saat memuat data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}