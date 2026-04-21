package com.example.watchmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchmobile.data.network.IdlixScraper
import com.example.watchmobile.data.network.NetworkModule
import com.example.watchmobile.domain.models.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var currentPage = 1
    private var hasMoreList = true

    init {
        fetchMovies()
    }

    fun refreshMovies() {
        currentPage = 1
        hasMoreList = true
        fetchMovies()
    }

    fun loadMoreMovies() {
        if (_isLoadingMore.value || _isLoading.value || !hasMoreList) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            try {
                val scrapedMovies = IdlixScraper.scrapeHomeMovies(currentPage)
                val finalMovies = if (scrapedMovies.isNotEmpty()) {
                    scrapedMovies
                } else {
                    runCatching { NetworkModule.idlixApiService.getMovies(currentPage).data }
                        .getOrDefault(emptyList())
                }

                if (finalMovies.isEmpty()) {
                    hasMoreList = false
                } else {
                    _movies.value = _movies.value + finalMovies
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // You might handle paginated error gracefully
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
                val scrapedMovies = IdlixScraper.scrapeHomeMovies(currentPage)
                val finalMovies = if (scrapedMovies.isNotEmpty()) {
                    scrapedMovies
                } else {
                    runCatching { NetworkModule.idlixApiService.getMovies(currentPage).data }
                        .getOrDefault(emptyList())
                }

                _movies.value = finalMovies
                if (finalMovies.isEmpty()) {
                    hasMoreList = false
                    _error.value = "Data tidak ditemukan. Coba ganti BASE_URL atau cek blokir jaringan."
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
