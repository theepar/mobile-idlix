package com.example.watchmobile.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

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

    // Initialize ExoPlayer with CacheDataSourceFactory
    val exoPlayer = remember {
        val trackSelector = androidx.media3.exoplayer.trackselection.DefaultTrackSelector(context)
        val dataSourceFactory = com.example.watchmobile.utils.DownloadUtil.getDataSourceFactory(context)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
        
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
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
        var isControllerVisible by remember { mutableStateOf(true) }
        
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        keepScreenOn = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowSubtitleButton(true)
                        
                        setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                            isControllerVisible = visibility == android.view.View.VISIBLE
                        })
                        
                        // Fullscreen toggle (rotasi)
                        setFullscreenButtonClickListener { isFullScreen ->
                            val activity = ctx as? android.app.Activity
                            if (isFullScreen) {
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Premium Overlay (Back, Title, Skip)
            androidx.compose.animation.AnimatedVisibility(
                visible = isControllerVisible,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar: Back & Title
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val activity = context as? android.app.Activity
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                (context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                            },
                            modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Streaming Movie", // Could pass actual title here
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Center: Skip Buttons (Optional, but "selayaknya stream")
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { exoPlayer.seekBack() },
                            modifier = Modifier.size(64.dp).background(Color.Black.copy(0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Replay10,
                                contentDescription = "Skip -10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Space for ExoPlayer's own play/pause button if not hiding it
                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(
                            onClick = { exoPlayer.seekForward() },
                            modifier = Modifier.size(64.dp).background(Color.Black.copy(0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Forward10,
                                contentDescription = "Skip +10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
