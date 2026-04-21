package com.example.watchmobile.data.network

import android.util.Log
import com.example.watchmobile.BuildConfig
import com.example.watchmobile.domain.models.Movie
import com.example.watchmobile.domain.models.MovieDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.UUID

object IdlixScraper {
    private const val TAG = "IdlixScraper"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    suspend fun scrapeHomeMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val movies = mutableListOf<Movie>()
        try {
            Log.d(TAG, "Connecting to: ${BuildConfig.BASE_URL}")
            val document = Jsoup.connect(BuildConfig.BASE_URL)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9,id;q=0.8")
                .timeout(15000)
                .get()

            Log.d(TAG, "Page title: ${document.title()}")
            Log.d(TAG, "Body length: ${document.body().html().length}")

            // Try broad selectors first - Next.js / WordPress streaming themes
            val selectors = listOf(
                "article.item",           // Dooplay/PsyPlay WordPress theme
                "div.item",               // Common streaming theme
                "div.post-item",          // Post-based layout
                "li.post-item",           // List-based layout
                "div[class*='movie']",    // Any div with 'movie' in class
                "div[class*='film']",     // Any div with 'film' in class
                "div[class*='item']",     // Any div with 'item' in class
                "div[class*='card']",     // Card-based layout (Next.js common)
                "a[href*='/movie/']",     // Direct movie links
                "a[href*='/film/']",      // Film links
                "a[href*='/series/']"     // Series links
            )

            var movieElements = document.select("article.item, div.item")
            Log.d(TAG, "article.item / div.item count: ${movieElements.size}")

            if (movieElements.isEmpty()) {
                for (selector in selectors) {
                    movieElements = document.select(selector)
                    Log.d(TAG, "Trying '$selector': found ${movieElements.size} elements")
                    if (movieElements.isNotEmpty()) break
                }
            }

            Log.d(TAG, "Total elements found: ${movieElements.size}")

            for (element in movieElements.take(20)) {
                // Title - try multiple selectors
                val titleEl = element.selectFirst("h3, h2, h1, .title, [class*='title'], [class*='name']")
                    ?: element.selectFirst("a")
                val title = titleEl?.text()?.trim()
                if (title.isNullOrBlank()) continue

                // URL / slug
                val aEl = element.selectFirst("a[href]") ?: element.closest("a[href]")
                val href = aEl?.attr("href") ?: ""
                val slug = href.trimEnd('/').substringAfterLast("/").ifBlank { title.lowercase().replace(" ", "-") }

                // Poster image
                val imgEl = element.selectFirst("img[src], img[data-src]")
                val posterPath = imgEl?.attr("src")?.takeIf { it.isNotBlank() }
                    ?: imgEl?.attr("data-src")

                // Quality badge
                val quality = element.selectFirst(".quality, .badge, [class*='quality'], [class*='badge'], [class*='hdtv']")
                    ?.text()?.takeIf { it.isNotBlank() } ?: "HD"

                // Rating
                val ratingRaw = element.selectFirst(".rating, .imdb, [class*='rating'], [class*='score'], [class*='imdb']")
                    ?.text()?.replace(Regex("[^0-9.]"), "")?.takeIf { it.isNotBlank() } ?: "0.0"

                // Year
                val year = element.selectFirst(".year, .date, [class*='year'], [class*='date']")?.text()

                Log.d(TAG, "Movie: $title | $slug | $posterPath")
                movies.add(
                    Movie(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        slug = slug,
                        posterPath = posterPath,
                        backdropPath = posterPath,
                        releaseDate = year,
                        voteAverage = ratingRaw,
                        viewCount = 0,
                        quality = quality,
                        contentType = if (href.contains("series")) "series" else "movie"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping home: ${e.message}", e)
        }
        Log.d(TAG, "Returning ${movies.size} movies")
        movies
    }

    suspend fun scrapeMovieDetail(slug: String): MovieDetail? = withContext(Dispatchers.IO) {
        try {
            // Try both /movie/ and direct slug path
            val urlsToTry = listOf(
                "${BuildConfig.BASE_URL}/movie/$slug",
                "${BuildConfig.BASE_URL}/$slug",
                "${BuildConfig.BASE_URL}/film/$slug"
            )

            var document: org.jsoup.nodes.Document? = null
            for (url in urlsToTry) {
                try {
                    Log.d(TAG, "Trying detail URL: $url")
                    document = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .timeout(15000)
                        .get()
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Failed $url: ${e.message}")
                }
            }

            if (document == null) return@withContext null

            val title = document.selectFirst("h1, .title, [class*='title']")?.text() ?: slug
            val imgEl = document.selectFirst(".poster img, .sbox img, .imagen img, [class*='poster'] img")
            val posterPath = imgEl?.attr("src") ?: ""
            val backdropPath = document.selectFirst("[class*='backdrop'] img, [class*='hero'] img")
                ?.attr("src") ?: posterPath
            val synopsis = document.selectFirst("[class*='description'], [class*='synopsis'], .wp-content p, #info p, .sbox p")
                ?.text() ?: "No synopsis available."
            val iframeEl = document.selectFirst("iframe[src]")
            val embedUrl = iframeEl?.attr("src")
            val cast = document.select("[class*='cast'] a, [class*='actor'], .castItem, .person")
                .map { it.text() }.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() } ?: listOf("Unknown")
            val genres = document.select("[class*='genre'] a, .sgeneros a, .genres a")
                .map { it.text() }.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() } ?: listOf("Movie")
            val quality = document.selectFirst(".quality, [class*='quality']")?.text() ?: "HD"
            val rating = document.selectFirst(".rating, .imdb, [class*='rating']")
                ?.text()?.replace(Regex("[^0-9.]"), "") ?: "0.0"
            val year = document.selectFirst(".date, .year, [class*='year']")?.text() ?: "N/A"

            Log.d(TAG, "Detail: $title | embed: $embedUrl | genres: $genres")

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
            Log.e(TAG, "Error scraping detail $slug: ${e.message}", e)
            null
        }
    }
}
