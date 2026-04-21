package com.example.watchmobile.data.network

import com.example.watchmobile.domain.models.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IdlixApiService {
    
    @GET("api/movies")
    suspend fun getMovies(@Query("page") page: Int = 1): MovieResponse

    @GET("api/movies/{slug}")
    suspend fun getMovieDetail(@Path("slug") slug: String): Any // Bisa diperbarui nanti

    @GET("api/search")
    suspend fun searchMovies(@Query("q") query: String): MovieResponse
}
