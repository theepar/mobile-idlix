package com.example.watchmobile.data.network

import com.example.watchmobile.BuildConfig
import com.example.watchmobile.domain.models.Movie
import com.example.watchmobile.domain.models.MovieDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.UUID

object IdlixScraper {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

    suspend fun scrapeHomeMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val movies = mutableListOf<Movie>()
        try {
            val document = Jsoup.connect(BuildConfig.BASE_URL)
                .userAgent(USER_AGENT)
                .get()

            // Select elements based on common WordPress streaming templates (Dooplay/PsyPlay)
            val movieElements = document.select("article.item, div.item, div.post")

            for (element in movieElements) {
                val titleElement = element.selectFirst("h3, .title, .data h3 a")
                val title = titleElement?.text() ?: continue
                
                val aElement = element.selectFirst("a")
                val url = aElement?.attr("href") ?: ""
                val slug = url.trimEnd('/').substringAfterLast("/")

                val imgElement = element.selectFirst("img")
                val posterPath = imgElement?.attr("src")

                val qualityElement = element.selectFirst(".quality, .m-quality")
                val quality = qualityElement?.text() ?: "HD"

                val ratingElement = element.selectFirst(".rating, .imdb")
                val rating = ratingElement?.text()?.replace(Regex("[^0-9.]"), "") ?: "-"

                val yearElement = element.selectFirst(".year")
                val year = yearElement?.text()

                movies.add(
                    Movie(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        slug = slug,
                        posterPath = posterPath,
                        backdropPath = posterPath,
                        releaseDate = year,
                        voteAverage = rating,
                        viewCount = 0,
                        quality = quality,
                        contentType = "movie"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        movies
    }

    suspend fun scrapeMovieDetail(slug: String): MovieDetail? = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect("${BuildConfig.BASE_URL}/movie/$slug")
                .userAgent(USER_AGENT)
                .get()

            val title = document.selectFirst("h1, .sbox h1, .data h1")?.text() ?: ""
            
            val imgElement = document.selectFirst(".poster img, .sbox .img img, .imagen img")
            val posterPath = imgElement?.attr("src") ?: ""
            val backdropPath = document.selectFirst(".g-item img, .backdrop img")?.attr("src") ?: posterPath

            val synopsis = document.selectFirst(".wp-content p, .sbox p, #info p")?.text() ?: "No synopsis available."
            
            val iframeElement = document.selectFirst("iframe")
            val embedUrl = iframeElement?.attr("src")
            
            val cast = document.select(".castItem, .person").map { it.text() }.takeIf { it.isNotEmpty() } ?: listOf("Unknown")
            val genres = document.select(".sgeneros a, .genres a").map { it.text() }.takeIf { it.isNotEmpty() } ?: listOf("Movie")
            
            val quality = document.selectFirst(".quality")?.text() ?: "HD"
            val rating = document.selectFirst(".rating, .imdb")?.text()?.replace(Regex("[^0-9.]"), "") ?: "-"
            val year = document.selectFirst(".date, .year")?.text() ?: "N/A"

            MovieDetail(
                id = UUID.randomUUID().toString(),
                title = title,
                slug = slug,
                posterPath = posterPath,
                backdropPath = backdropPath,
                releaseDate = year,
                voteAverage = rating,
                quality = quality,
                synopsis = synopsis,
                cast = cast,
                genres = genres,
                embedUrl = embedUrl
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
