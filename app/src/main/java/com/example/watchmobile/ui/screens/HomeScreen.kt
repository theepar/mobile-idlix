package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.watchmobile.domain.models.Movie
import com.example.watchmobile.ui.theme.DarkSurface
import com.example.watchmobile.ui.theme.IdlixRed
import com.example.watchmobile.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            if (isLoading && movies.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = IdlixRed)
            } else if (errorMsg != null && movies.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.refreshMovies() },
                        colors = ButtonDefaults.buttonColors(containerColor = IdlixRed)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else if (movies.isNotEmpty()) {
                val heroMovie = movies.first()
                val trendingMovies = movies.drop(1).take(10)
                val forYouMovies = movies.drop(11)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Non-sticky Header with deeper gradient
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp) // Deeper black fade
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black, Color.Black.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        ) {
                            Text(
                                text = "IDLIX",
                                color = IdlixRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    // Hero Section (Shifted up to be behind the gradient)
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(550.dp) // Taller hero
                                .padding(top = (-120).dp) // Negative padding to slide under header
                                .clickable { onMovieClick(heroMovie.slug) }
                        ) {
                            AsyncImage(
                                model = heroMovie.fullPosterUrl,
                                contentDescription = heroMovie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Bottom fade for hero
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f), Color.Black),
                                            startY = 400f
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = heroMovie.title,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = heroMovie.year, color = Color.Gray, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        color = IdlixRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, IdlixRed.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = heroMovie.quality ?: "HD",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(
                                        onClick = { onPlayClick(heroMovie.slug) },
                                        colors = ButtonDefaults.buttonColors(containerColor = IdlixRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp).width(120.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Play", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onMovieClick(heroMovie.slug) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp).width(120.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = "Detail", tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Detail", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Trending Section
                    if (trendingMovies.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Trending Now",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Scrollable Row for Trending
                        item(span = { GridItemSpan(2) }) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trendingMovies) { movie ->
                                    MovieCard(movie = movie, onClick = { onMovieClick(movie.slug) })
                                }
                            }
                        }
                    }

                    // For You Section
                    if (forYouMovies.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "For You",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(forYouMovies) { movie ->
                            Box(modifier = Modifier.padding(8.dp)) {
                                MovieCard(
                                    movie = movie,
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    onClick = { onMovieClick(movie.slug) }
                                )
                            }
                        }
                        
                        item(span = { GridItemSpan(2) }) {
                            LaunchedEffect(key1 = true) {
                                viewModel.loadMoreMovies()
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(color = IdlixRed, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, modifier: Modifier = Modifier.width(120.dp).height(180.dp), onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(DarkSurface)
    ) {
        AsyncImage(
            model = movie.fullPosterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (!movie.quality.isNullOrEmpty()) {
            Text(
                text = movie.quality,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IdlixRed)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
