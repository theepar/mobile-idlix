package com.example.watchmobile.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    videoUrl: String,
    subtitleUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(videoUrl, subtitleUrl) {
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))

        // Jika ada subtitle (.vtt/.srt), tambahkan sebagai side-loaded track
        if (!subtitleUrl.isNullOrEmpty()) {
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl))
                .setMimeType(
                    if (subtitleUrl.endsWith(".vtt", ignoreCase = true)) MimeTypes.TEXT_VTT
                    else MimeTypes.APPLICATION_SUBRIP
                )
                .setLanguage("id")
                .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                .build()

            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        }

        val mediaItem = mediaItemBuilder.build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                keepScreenOn = true
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
