package com.example.watchmobile.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Pattern

data class MediaData(
    val title: String = "",
    val videoResolutions: Map<String, String> = emptyMap(), // ex: "720p" -> "https://..."
    val subtitles: List<Subtitle> = emptyList(),
    val rawM3u8Url: String? = null
)

data class Subtitle(
    val label: String,
    val file: String,
    val kind: String = "captions"
)

object MediaResolver {
    
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

    /**
     * Resolves the embed URL to extract M3U8 manifest and Subtitles.
     */
    suspend fun resolveMedia(embedUrl: String): Result<MediaData> = withContext(Dispatchers.IO) {
        try {
            // Replace IDLIX host with our Cloudflare Worker proxy if it's an IDLIX URL
            var finalUrl = embedUrl
            if (finalUrl.startsWith("/")) {
                finalUrl = com.example.watchmobile.BuildConfig.BASE_URL + finalUrl
            } else if (finalUrl.contains("idlix")) {
                try {
                    val originalHost = java.net.URL(finalUrl).host
                    val proxyHost = java.net.URL(com.example.watchmobile.BuildConfig.BASE_URL).host
                    finalUrl = finalUrl.replace(originalHost, proxyHost)
                } catch (e: Exception) {
                    // Ignore URL parsing errors
                }
            }

            android.util.Log.d("MediaResolver", "Resolving embed URL: $finalUrl")

            // Fetch the HTML source of the embed/iframe URL
            val userAgentToUse = com.example.watchmobile.utils.CloudflareBypasser.bypassedUserAgent

            val connection = Jsoup.connect(finalUrl)
                .userAgent(userAgentToUse)
                .header("User-Agent", userAgentToUse)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9,id;q=0.8")
                .referrer(com.example.watchmobile.BuildConfig.BASE_URL)
                .ignoreContentType(true)
                .timeout(15000)

            val cookiesStr = com.example.watchmobile.utils.CloudflareBypasser.getCookies()
            if (cookiesStr.isNotEmpty()) {
                for (cookie in cookiesStr.split("; ")) {
                    val split = cookie.split("=", limit = 2)
                    if (split.size == 2) {
                        connection.cookie(split[0].trim(), split[1].trim())
                    }
                }
            }

            val document = connection.get()
            val html = document.html()
            
            // 1. Ekstrak M3U8 URL (Biasanya dari variabel JS atau tag source)
            val m3u8Url = extractM3u8Url(html)
            
            // 2. Ekstrak Subtitles
            val subtitles = extractSubtitles(html)
            
            if (m3u8Url != null) {
                Result.success(
                    MediaData(
                        title = document.title(),
                        rawM3u8Url = m3u8Url,
                        subtitles = subtitles
                    )
                )
            } else {
                Result.failure(Exception("M3U8 URL not found in the embed page."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractM3u8Url(html: String): String? {
        android.util.Log.d("MediaResolver", "Extracting m3u8 from html length: ${html.length}")
        
        // RegEx ini mencoba mencari URL berakhiran .m3u8
        val m3u8Pattern = Pattern.compile("(https?:\\\\/\\\\/[^\"]+\\\\.m3u8[^\"]*)")
        val matcher = m3u8Pattern.matcher(html)
        if (matcher.find()) {
            val url = matcher.group(1)?.replace("\\/", "/")
            android.util.Log.d("MediaResolver", "Found m3u8 via direct regex: $url")
            return url
        }
        
        // Pencarian alternatif: mencari 'file: "http...'
        val filePattern = Pattern.compile("file\\s*:\\s*[\"'](https?://[^\"]+\\.m3u8[^\"]*)[\"']")
        val fileMatcher = filePattern.matcher(html)
        if (fileMatcher.find()) {
            val url = fileMatcher.group(1)
            android.util.Log.d("MediaResolver", "Found m3u8 via file: pattern: $url")
            return url
        }
        
        // Coba cari ekstensi .mp4
        val mp4Pattern = Pattern.compile("(https?:\\\\/\\\\/[^\"]+\\\\.mp4[^\"]*)")
        val mp4Matcher = mp4Pattern.matcher(html)
        if (mp4Matcher.find()) {
            val url = mp4Matcher.group(1)?.replace("\\/", "/")
            android.util.Log.d("MediaResolver", "Found mp4 fallback: $url")
            return url
        }
        
        android.util.Log.d("MediaResolver", "No m3u8/mp4 found in HTML")
        return null
    }

    private fun extractSubtitles(html: String): List<Subtitle> {
        val subtitles = mutableListOf<Subtitle>()
        
        // Coba ekstrak dari tag <track>
        val document = Jsoup.parse(html)
        val tracks = document.select("track[kind=captions], track[kind=subtitles]")
        for (track in tracks) {
            val label = track.attr("label")
            val src = track.attr("src")
            if (src.isNotEmpty()) {
                subtitles.add(Subtitle(label = label.ifEmpty { "Unknown" }, file = src))
            }
        }
        
        // Jika tidak ada di tag HTML, cari di string JSON/Javascript array
        if (subtitles.isEmpty()) {
            val srtPattern = Pattern.compile("(https?:\\\\/\\\\/[^\"]+\\\\.(vtt|srt)[^\"]*)")
            val matcher = srtPattern.matcher(html)
            var count = 1
            while (matcher.find()) {
                val url = matcher.group(1)?.replace("\\/", "/")
                if (url != null && !subtitles.any { it.file == url }) {
                    subtitles.add(Subtitle(label = "Sub \$count", file = url))
                    count++
                }
            }
        }
        return subtitles
    }
}
