package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
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
fun DiscoverScreen(
    onMovieClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    val categories = remember(movies) {
        listOf("All") + movies
            .mapNotNull { it.contentType }
            .map { it.replaceFirstChar { c -> c.uppercase() } }
            .distinct()
    }
    var selectedCategory by remember(categories) { mutableStateOf("All") }

    val filteredMovies = remember(movies, searchQuery, selectedCategory) {
        movies.filter { movie ->
            val queryOk = searchQuery.isBlank() ||
                movie.title.contains(searchQuery, ignoreCase = true)
            val categoryOk = selectedCategory == "All" ||
                movie.contentType?.equals(selectedCategory, ignoreCase = true) == true
            queryOk && categoryOk
        }
    }

    val featuredMovies = filteredMovies.take(10)
    val recommendedMovies = filteredMovies.drop(10)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search movies, TV shows...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = IdlixRed,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) IdlixRed else DarkSurface)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = IdlixRed
                        )
                    }
                }

                errorMsg != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refreshMovies() },
                            colors = ButtonDefaults.buttonColors(containerColor = IdlixRed)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }

                filteredMovies.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Data belum tersedia atau filter terlalu ketat.", color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refreshMovies() },
                            colors = ButtonDefaults.buttonColors(containerColor = IdlixRed)
                        ) {
                            Text("Muat Ulang")
                        }
                    }
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Text(
                            text = "Top This Year",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredMovies) { movie ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurface)
                                        .clickable { onMovieClick(movie.slug) }
                                ) {
                                    AsyncImage(
                                        model = movie.fullPosterUrl,
                                        contentDescription = movie.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        Text(
                            text = "Recommended for You",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(if (recommendedMovies.isEmpty()) featuredMovies else recommendedMovies) { movie ->
                        MovieListItem(
                            movie = movie,
                            onClick = { onMovieClick(movie.slug) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieListItem(movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = movie.fullPosterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = movie.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${movie.contentType ?: "Movie"} • ${movie.year}",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
