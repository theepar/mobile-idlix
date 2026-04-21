package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.widget.Toast
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.example.watchmobile.services.DemoDownloadService
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.watchmobile.ui.theme.DarkSurface
import com.example.watchmobile.ui.theme.IdlixRed
import com.example.watchmobile.ui.viewmodels.MovieDetailViewModel

@Composable
fun MovieDetailScreen(
    slug: String,
    onPlayClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: MovieDetailViewModel = viewModel()
) {
    LaunchedEffect(slug) {
        viewModel.fetchMovieDetail(slug)
    }

    val detail by viewModel.movieDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showResolutionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = IdlixRed)
        }
        return
    }

    if (detail == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Movie not found.", color = Color.White)
        }
        return
    }

    val movie = detail!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = movie.fullBackdropUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 100f
                        )
                    )
            )
            
            // Back Button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = Color.White, fontSize = 20.sp)
            }
        }

        // Details content
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(movie.year, color = Color.Gray, fontSize = 14.sp)
                Text("•", color = Color.Gray, fontSize = 14.sp)
                Text(movie.quality ?: "HD", color = IdlixRed, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White).padding(horizontal = 4.dp, vertical = 2.dp))
                Text("•", color = Color.Gray, fontSize = 14.sp)
                Text("⭐ ${movie.voteAverage}", color = Color(0xFFFFA000), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Play Button
            Button(
                onClick = { 
                    val embed = movie.embedUrl ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                    onPlayClick(embed) 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IdlixRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("▶ Play Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Download Button
            OutlinedButton(
                onClick = { showResolutionDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("↓ Download", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            if (showResolutionDialog) {
                AlertDialog(
                    onDismissRequest = { showResolutionDialog = false },
                    title = { Text("Select Resolution") },
                    text = {
                        Column {
                            listOf("1080p (FHD)", "720p (HD)", "480p (SD)", "360p (Low)").forEach { res ->
                                TextButton(
                                    onClick = {
                                        showResolutionDialog = false
                                        val url = movie.embedUrl ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                                        val downloadRequest = DownloadRequest.Builder(movie.id, Uri.parse(url))
                                            .setCustomCacheKey(movie.id + "_" + res)
                                            .setData(movie.title.toByteArray())
                                            .build()
                                            
                                        DownloadService.sendAddDownload(
                                            context,
                                            DemoDownloadService::class.java,
                                            downloadRequest,
                                            true
                                        )
                                        Toast.makeText(context, "Download queued: $res", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(res, color = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showResolutionDialog = false }) {
                            Text("Cancel", color = IdlixRed)
                        }
                    },
                    containerColor = DarkSurface,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Synopsis", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(movie.synopsis, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Cast", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(movie.cast.joinToString(", "), color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
