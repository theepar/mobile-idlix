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

    private fun fetchDocument(url: String, referer: String? = BuildConfig.BASE_URL): org.jsoup.nodes.Document {
        val userAgentToUse = com.example.watchmobile.utils.CloudflareBypasser.bypassedUserAgent

        val connection = Jsoup.connect(url)
            .userAgent(userAgentToUse)
            .header("User-Agent", userAgentToUse)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9,id;q=0.8")
            .header("Cache-Control", "max-age=0")
            .header("Upgrade-Insecure-Requests", "1")
            .timeout(15000)
            .ignoreHttpErrors(true)
            .followRedirects(true)

        if (!referer.isNullOrBlank()) {
            connection.referrer(referer)
        }

        val cookiesStr = com.example.watchmobile.utils.CloudflareBypasser.getCookies()
        if (cookiesStr.isNotEmpty()) {
            Log.d(TAG, "Using cookies for $url: ${cookiesStr.take(50)}...")
            for (cookie in cookiesStr.split("; ")) {
                val split = cookie.split("=", limit = 2)
                if (split.size == 2) {
                    connection.cookie(split[0].trim(), split[1].trim())
                }
            }
        }

        return connection.get()
    }

    private suspend fun scrapeMoviesFromUrl(url: String): List<Movie> = withContext(Dispatchers.IO) {
        val movies = mutableListOf<Movie>()
        try {
            Log.d(TAG, "Connecting to: $url")
            val document = fetchDocument(url)

            val selectors = listOf(
                "a.content-card",          // Current IDLIX Structure
                "article.item-movies",     // Legacy IDLIX Home
                "article.item",            // Legacy IDLIX Genre
                "div.item",
                "div.post-item",
                "div[class*='movie']",
                "div[class*='film']",
                "div[class*='card']"
            )

            var movieElements = document?.select("a.content-card, article.item-movies, article.item, div.item, .post-item, .ml-item") ?: org.jsoup.select.Elements()
            if (movieElements.isEmpty()) {
                for (selector in selectors) {
                    movieElements = document?.select(selector) ?: org.jsoup.select.Elements()
                    if (movieElements.isNotEmpty()) {
                        Log.d(TAG, "Found elements using selector: $selector")
                        break
                    }
                }
            }
            
            Log.d(TAG, "Found ${movieElements.size} movie elements on page $url")

            for (element in movieElements) {
                // Title
                val titleEl = element.selectFirst("h3, h2, h1, .title, [class*='title'], [class*='name']")
                    ?: element.selectFirst("a")
                val title = titleEl?.text()?.trim() ?: element.attr("title")
                if (title.isBlank()) continue

                // URL / slug
                val aEl = element.selectFirst("a[href]") ?: element.closest("a[href]")
                val href = aEl?.attr("href") ?: ""
                val fullPath = href.replace(BuildConfig.BASE_URL, "").trim('/')
                val slug = if (fullPath.contains("/")) fullPath else fullPath.substringAfterLast("/")
                if (slug.isBlank()) continue

                // Poster image
                val imgEl = element.selectFirst("img[src], img[data-src]")
                val posterPath = imgEl?.attr("src")?.takeIf { it.isNotBlank() }
                    ?: imgEl?.attr("data-src")

                // Quality
                val quality = element.selectFirst(".quality, .badge, [class*='quality'], [class*='badge'], [class*='hdtv']")
                    ?.text()?.takeIf { it.isNotBlank() } ?: "HD"

                // Rating
                val ratingRaw = element.selectFirst(".rating, .imdb, [class*='rating'], [class*='score'], [class*='imdb']")
                    ?.text()?.replace(Regex("[^0-9.]"), "")?.takeIf { it.isNotBlank() } ?: "0.0"

                // Year
                val year = element.selectFirst(".year, .date, [class*='year'], [class*='date']")?.text()

                // Genre from text if exists
                val genre = element.selectFirst(".genre, .genres, [class*='genre']")?.text()

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
                        contentType = genre ?: (if (href.contains("series")) "series" else "movie")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping url $url: ${e.message}", e)
        }
        movies
    }

    suspend fun scrapeHomeMovies(page: Int = 1): List<Movie> {
        val url = if (page > 1) "${BuildConfig.BASE_URL}/page/$page/" else BuildConfig.BASE_URL
        return scrapeMoviesFromUrl(url).take(20) // Home usually needs limited but Discover can take all
    }

    private val genreMapping = mapOf(
        "aksi" to "action",
        "animasi" to "animation",
        "horor" to "horror",
        "romantis" to "romance",
        "kriminal" to "crime",
        "misteri" to "mystery",
        "petualangan" to "adventure",
        "komedi" to "comedy",
        "keluarga" to "family",
        "fantasi" to "fantasy",
        "perang" to "war",
        "sci-fi" to "sci-fi",
        "drama" to "drama"
    )

    suspend fun scrapeCategoryMovies(category: String, page: Int = 1, searchQuery: String = ""): List<Movie> {
        val baseUrl = com.example.watchmobile.BuildConfig.BASE_URL
        
        // Handle "Semua" specially to match Home exactly
        if (searchQuery.isBlank() && category.equals("Semua", ignoreCase = true)) {
            return scrapeHomeMovies(page)
        }

        var url = when {
            searchQuery.isNotBlank() -> {
                "$baseUrl/?s=$searchQuery"
            }
            category.equals("Series", ignoreCase = true) || category.equals("TV Series", ignoreCase = true) -> {
                "$baseUrl/series/"
            }
            else -> {
                val genreSlug = genreMapping[category.lowercase()] ?: category.lowercase().replace(" ", "-")
                "$baseUrl/genre/$genreSlug/"
            }
        }

        // Add pagination
        if (page > 1) {
            url = if (url.contains("?s=")) {
                "$url&paged=$page"
            } else {
                "${url.trimEnd('/')}/page/$page/"
            }
        }
        
        Log.d(TAG, "Scraping Discover URL: $url")
        return scrapeMoviesFromUrl(url)
    }

    suspend fun scrapeMovieDetail(slug: String): MovieDetail? = withContext(Dispatchers.IO) {
        try {
            // Try both /movie/, /series/, and direct slug path
            val urlsToTry = listOf(
                "${com.example.watchmobile.BuildConfig.BASE_URL}/movie/$slug",
                "${com.example.watchmobile.BuildConfig.BASE_URL}/series/$slug",
                "${com.example.watchmobile.BuildConfig.BASE_URL}/tv/$slug",
                "${com.example.watchmobile.BuildConfig.BASE_URL}/$slug",
                "${com.example.watchmobile.BuildConfig.BASE_URL}/film/$slug"
            )

            var document: org.jsoup.nodes.Document? = null
            for (url in urlsToTry) {
                try {
                    Log.d(TAG, "Trying detail URL: $url")
                    document = fetchDocument(url)
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Failed $url: ${e.message}")
                }
            }

            if (document == null) return@withContext null

            val title = document.selectFirst(".data h1, .title h1, h1, .title")?.text() ?: slug
            val imgEl = document.selectFirst(".poster img, .sbox img, .imagen img, [class*='poster'] img")
            val posterPath = imgEl?.attr("src") ?: ""
            val backdropPath = document.selectFirst("[class*='backdrop'] img, [class*='hero'] img, .imagen img")
                ?.attr("src") ?: posterPath
            val synopsis = document.selectFirst(".wp-content p, .description p, .synopsis p, .sbox p, #info p")
                ?.text() ?: "No synopsis available."
            
            // Try to find the first iframe for the video player
            val iframeEl = document.selectFirst(".player-box iframe, .p-box iframe, iframe[src*='embed'], .video-content iframe") 
                ?: document.selectFirst("iframe[src]")
            val embedUrl = iframeEl?.attr("src")
            
            val cast = document.select(".persons .person a, [class*='cast'] a, .castItem a, .starring a")
                .map { it.text() }.filter { it.isNotBlank() }.distinct().takeIf { it.isNotEmpty() } ?: listOf("N/A")
            
            val genres = document.select(".sgeneros a, .genres a, [class*='genre'] a, .category a")
                .map { it.text() }.filter { it.isNotBlank() }.distinct().takeIf { it.isNotEmpty() } ?: listOf("Movie")
            
            val quality = document.selectFirst(".quality, [class*='quality'], .badge-quality")?.text() ?: "HD"
            val rating = document.selectFirst(".rating, .imdb, .score, .num")
                ?.text()?.replace(Regex("[^0-9.]"), "") ?: "0.0"
            val year = document.selectFirst(".date, .year, .release, .released")?.text() ?: "N/A"

            Log.d(TAG, "Scraped Detail Success: $title | embed: $embedUrl")

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
