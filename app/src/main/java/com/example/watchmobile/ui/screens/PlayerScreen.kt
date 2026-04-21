package com.example.watchmobile.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.watchmobile.domain.MediaResolver
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    videoUrl: String, // Ini awalnya adalah Embed URL
    subtitleUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var resolvedM3u8Url by remember { mutableStateOf<String?>(null) }
    var resolvedSubtitles by remember { mutableStateOf<List<com.example.watchmobile.domain.Subtitle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(videoUrl) {
        isLoading = true
        // Jika URL sudah berupa m3u8 (misal dummy), tidak perlu di-resolve
        if (videoUrl.contains(".m3u8") || videoUrl.contains(".mp4")) {
            resolvedM3u8Url = videoUrl
            isLoading = false
        } else {
            coroutineScope.launch {
                val result = MediaResolver.resolveMedia(videoUrl)
                result.onSuccess { data ->
                    resolvedM3u8Url = data.rawM3u8Url
                    resolvedSubtitles = data.subtitles
                    isLoading = false
                }.onFailure {
                    errorMessage = "Failed to extract video stream."
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(resolvedM3u8Url) {
        resolvedM3u8Url?.let { m3u8 ->
            val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(m3u8))

            // Load subtitle jika ada
            val subUrl = subtitleUrl ?: resolvedSubtitles.firstOrNull()?.file
            if (!subUrl.isNullOrEmpty()) {
                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subUrl))
                    .setMimeType(
                        if (subUrl.endsWith(".vtt", ignoreCase = true)) MimeTypes.TEXT_VTT
                        else MimeTypes.APPLICATION_SUBRIP
                    )
                    .setLanguage("id")
                    .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                    .build()

                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
            }

            exoPlayer.setMediaItem(mediaItemBuilder.build())
            exoPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Red)
        }
    } else if (errorMessage != null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(errorMessage!!, color = Color.White)
        }
    } else {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    keepScreenOn = true
                }
            },
            modifier = modifier.fillMaxSize().background(Color.Black)
        )
    }
}
