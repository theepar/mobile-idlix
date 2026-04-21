package com.example.watchmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchmobile.data.network.IdlixScraper
import com.example.watchmobile.domain.models.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel : ViewModel() {
    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchMovieDetail(slug: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val detail = IdlixScraper.scrapeMovieDetail(slug)
                if (detail != null) {
                    _movieDetail.value = detail
                } else {
                    _error.value = "Failed to load movie details."
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
