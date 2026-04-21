package com.example.watchmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch data riil dari API
                val response = NetworkModule.idlixApiService.getMovies(page = 1)
                _movies.value = response.data
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "Terjadi kesalahan saat memuat data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
