package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MenuBottomSheet
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.StreamLinksScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MoviesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MoviesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Throwable) {
        }
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MoviesViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val searchUiState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val streamLinksUiState by viewModel.streamLinksUiState.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeVideoKey by viewModel.activeVideoKey.collectAsStateWithLifecycle()
    val activeDirectUrl by viewModel.activeDirectUrl.collectAsStateWithLifecycle()
    val activeVideoTitle by viewModel.activeVideoTitle.collectAsStateWithLifecycle()

    var showMenuSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    uiState = homeUiState,
                    currentLanguage = currentLanguage,
                    onSearchClick = { viewModel.navigateToSearch() },
                    onLanguageClick = { viewModel.toggleLanguage() },
                    onMenuClick = { showMenuSheet = true },
                    onMuteToggle = { viewModel.toggleHeroMute() },
                    onPlayHero = { hero -> viewModel.playMediaDefault(hero) },
                    onMoreInfoHero = { hero -> viewModel.navigateToDetail(hero) },
                    onMovieClick = { movie -> viewModel.navigateToDetail(movie, isTv = false) },
                    onTvClick = { tv -> viewModel.navigateToDetail(tv, isTv = true) },
                    onRetry = { viewModel.loadHomeData() }
                )
            }

            AppScreen.DETAIL -> {
                DetailScreen(
                    detail = detailUiState.mediaDetail,
                    isLoading = detailUiState.isLoading,
                    error = detailUiState.error,
                    isFavorite = detailUiState.isFavorite,
                    onBackClick = { viewModel.navigateToHome() },
                    onToggleFavorite = { viewModel.toggleFavoriteCurrent() },
                    onPlayClick = {
                        viewModel.openStreamScreenFromDetail()
                    },
                    onWatchTrailerClick = {
                        val detail = detailUiState.mediaDetail
                        if (detail != null) {
                            viewModel.playVideo(detail.youtubeTrailerKey, "${detail.displayTitle} - Trailer")
                        }
                    },
                    onSimilarClick = { item -> viewModel.navigateToDetail(item) }
                )
            }

            AppScreen.STREAM_LINKS -> {
                StreamLinksScreen(
                    uiState = streamLinksUiState,
                    onBackClick = { viewModel.navigateToHome() },
                    onLinkClick = { link ->
                        viewModel.playDirectStream(link.url, link.title)
                    },
                    onSearchAgain = { query ->
                        viewModel.loadDramaLinks(query)
                    }
                )
            }

            AppScreen.SEARCH -> {
                SearchScreen(
                    searchState = searchUiState,
                    onQueryChanged = { query -> viewModel.onSearchQueryChanged(query) },
                    onCategoryChanged = { cat -> viewModel.setSearchCategory(cat) },
                    onItemClick = { item -> viewModel.navigateToDetail(item) },
                    onBackClick = { viewModel.navigateToHome() }
                )
            }

            AppScreen.WATCHLIST -> {
                WatchlistScreen(
                    watchlist = watchlist,
                    onItemClick = { item -> viewModel.navigateToDetail(item) },
                    onRemoveClick = { item -> viewModel.toggleFavorite(item) },
                    onBackClick = { viewModel.navigateToHome() }
                )
            }
        }
        }

        // Hamburger Menu BottomSheet
        if (showMenuSheet) {
            MenuBottomSheet(
                onDismiss = { showMenuSheet = false },
                onNavigateHome = { viewModel.navigateToHome() },
                onNavigateSearch = { viewModel.navigateToSearch() },
                onNavigateWatchlist = { viewModel.navigateToWatchlist() },
                onToggleLanguage = { viewModel.toggleLanguage() },
                currentLanguage = currentLanguage
            )
        }

        // In-App Video & Trailer Player
        if (activeVideoKey != null || activeDirectUrl != null) {
            VideoPlayerDialog(
                videoKey = activeVideoKey,
                directUrl = activeDirectUrl,
                title = activeVideoTitle,
                onDismiss = { viewModel.closeVideoPlayer() }
            )
        }
    }
}
