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
            // Fetch the HTML source of the embed/iframe URL
            val document = Jsoup.connect(embedUrl)
                .userAgent(USER_AGENT)
                .referrer("https://z1.idlixku.com/")
                .ignoreContentType(true)
                .get()

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
        // RegEx ini mencoba mencari URL berakhiran .m3u8
        // Pola ini mungkin perlu disesuaikan dengan player spesifik (misal Fembed/JWPlayer)
        val m3u8Pattern = Pattern.compile("(https?:\\\\/\\\\/[^\"]+\\\\.m3u8[^\"]*)")
        val matcher = m3u8Pattern.matcher(html)
        if (matcher.find()) {
            return matcher.group(1)?.replace("\\/", "/")
        }
        
        // Pencarian alternatif: mencari 'file: "http...' jika dikemas dalam object
        val filePattern = Pattern.compile("file\\s*:\\s*[\"'](https?://[^\"]+\\.m3u8[^\"]*)[\"']")
        val fileMatcher = filePattern.matcher(html)
        if (fileMatcher.find()) {
             return fileMatcher.group(1)
        }
        
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
