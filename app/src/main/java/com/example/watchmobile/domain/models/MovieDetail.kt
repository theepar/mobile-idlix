package com.example.watchmobile.domain.models

data class MovieDetail(
    val id: String,
    val title: String,
    val slug: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: String?,
    val quality: String?,
    val synopsis: String,
    val cast: List<String>,
    val genres: List<String>,
    val embedUrl: String?
) {
    val fullPosterUrl: String
        get() = if (posterPath != null && posterPath.startsWith("/")) {
            "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
            posterPath ?: ""
        }
        
    val fullBackdropUrl: String
        get() = if (backdropPath != null && backdropPath.startsWith("/")) {
            "https://image.tmdb.org/t/p/w1280$backdropPath"
        } else {
            backdropPath ?: ""
        }
        
    val year: String
        get() = releaseDate?.take(4) ?: "N/A"
}
