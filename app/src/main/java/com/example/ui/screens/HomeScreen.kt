package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaItem
import com.example.ui.components.*
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    currentLanguage: String,
    onSearchClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMuteToggle: () -> Unit,
    onPlayHero: (MediaItem) -> Unit,
    onMoreInfoHero: (MediaItem) -> Unit,
    onMovieClick: (MediaItem) -> Unit,
    onTvClick: (MediaItem) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading && uiState.heroMedia == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CinemaRed)
            }
        } else if (uiState.error != null && uiState.heroMedia == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = uiState.error,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Top App Bar
                item {
                    TopHeaderBar(
                        currentLanguage = currentLanguage,
                        onSearchClick = onSearchClick,
                        onLanguageClick = onLanguageClick,
                        onMenuClick = onMenuClick,
                        modifier = Modifier.statusBarsPadding()
                    )
                }

                // Hero Banner matching Screenshot 1
                item {
                    HeroBanner(
                        mediaItem = uiState.heroMedia,
                        isMuted = uiState.isMuted,
                        onMuteToggle = onMuteToggle,
                        onPlayClick = {
                            uiState.heroMedia?.let { onPlayHero(it) }
                        },
                        onMoreInfoClick = {
                            uiState.heroMedia?.let { onMoreInfoHero(it) }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Top Movies Carousel matching Screenshot 2 (Rank 1, 2, 3...)
                item {
                    TopMoviesCarousel(
                        movies = uiState.topMovies,
                        onMovieClick = onMovieClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Trending Now Carousel matching Screenshot 2
                item {
                    MediaRowCarousel(
                        title = if (currentLanguage == "ar-SA") "الرائج الآن" else "Trending Now",
                        items = uiState.trendingMovies,
                        onItemClick = onMovieClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Popular TV Series
                item {
                    MediaRowCarousel(
                        title = if (currentLanguage == "ar-SA") "مسلسلات مشهورة" else "Popular TV Series",
                        items = uiState.popularTv,
                        onItemClick = onTvClick,
                        isTv = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Now Playing
                item {
                    MediaRowCarousel(
                        title = if (currentLanguage == "ar-SA") "يعرض حالياً في السينما" else "Now Playing in Theaters",
                        items = uiState.nowPlaying,
                        onItemClick = onMovieClick
                    )
                }
            }
        }
    }
}
