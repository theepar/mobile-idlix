package com.example.watchmobile.domain.models

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val data: List<Movie>,
    val pagination: Pagination?
)

data class Movie(
    val id: String,
    val title: String,
    val slug: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: String?,
    val viewCount: Int?,
    val quality: String?,
    val contentType: String?
) {
    // Helper untuk mendapatkan full URL gambar (asumsi menggunakan TMDB image server)
    val fullPosterUrl: String
        get() = when {
            posterPath == null -> ""
            posterPath.startsWith("http") -> posterPath
            posterPath.startsWith("//") -> "https:$posterPath"
            posterPath.startsWith("/") -> {
                // Check if it's likely a TMDB path (usually starts with /v/ or /p/)
                if (posterPath.contains("/t/p/") || posterPath.length < 40) {
                     "https://image.tmdb.org/t/p/w500$posterPath"
                } else {
                    "${com.example.watchmobile.BuildConfig.BASE_URL}$posterPath"
                }
            }
            else -> posterPath
        }
        
    val year: String
        get() = releaseDate?.take(4) ?: "N/A"
}

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
