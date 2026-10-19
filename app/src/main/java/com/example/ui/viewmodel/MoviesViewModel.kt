package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.DramaLink
import com.example.data.api.QDramaScraper
import com.example.data.model.MediaDetail
import com.example.data.model.MediaItem
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    DETAIL,
    SEARCH,
    WATCHLIST,
    STREAM_LINKS
}

enum class SearchCategory {
    ALL,
    MOVIES,
    TV
}

data class HomeUiState(
    val heroMedia: MediaItem? = null,
    val topMovies: List<MediaItem> = emptyList(),
    val trendingMovies: List<MediaItem> = emptyList(),
    val popularTv: List<MediaItem> = emptyList(),
    val nowPlaying: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isMuted: Boolean = true
)

data class DetailUiState(
    val mediaDetail: MediaDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)

data class SearchUiState(
    val query: String = "",
    val results: List<MediaItem> = emptyList(),
    val isSearching: Boolean = false,
    val category: SearchCategory = SearchCategory.ALL
)

data class StreamLinksUiState(
    val mediaItem: MediaItem? = null,
    val mediaDetail: MediaDetail? = null,
    val links: List<DramaLink> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeQuery: String = ""
)

class MoviesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovieRepository(application.applicationContext)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en-US")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(DetailUiState())
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _streamLinksUiState = MutableStateFlow(StreamLinksUiState())
    val streamLinksUiState: StateFlow<StreamLinksUiState> = _streamLinksUiState.asStateFlow()

    val watchlist: StateFlow<List<MediaItem>> = repository.watchlistFlow

    // Video Player state
    private val _activeVideoKey = MutableStateFlow<String?>(null)
    val activeVideoKey: StateFlow<String?> = _activeVideoKey.asStateFlow()

    private val _activeDirectUrl = MutableStateFlow<String?>(null)
    val activeDirectUrl: StateFlow<String?> = _activeDirectUrl.asStateFlow()

    private val _activeVideoTitle = MutableStateFlow<String>("")
    val activeVideoTitle: StateFlow<String> = _activeVideoTitle.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            try {
                _homeUiState.value = _homeUiState.value.copy(isLoading = true, error = null)
                val lang = _currentLanguage.value

                val trendingRes = repository.getTrendingMovies(lang)
                val topRatedRes = repository.getTopRatedMovies(lang)
                val trendingTvRes = repository.getTrendingTv(lang)
                val nowPlayingRes = repository.getNowPlaying(lang)

                val trendingList = trendingRes.getOrDefault(emptyList()).ifEmpty { repository.getFallbackMovies() }
                val topRatedList = topRatedRes.getOrDefault(emptyList()).ifEmpty { repository.getFallbackMovies() }
                val tvList = trendingTvRes.getOrDefault(emptyList()).ifEmpty { repository.getFallbackMovies() }
                val nowPlayingList = nowPlayingRes.getOrDefault(emptyList()).ifEmpty { repository.getFallbackMovies() }

                val hero = trendingList.firstOrNull { !it.backdropPath.isNullOrEmpty() }
                    ?: topRatedList.firstOrNull() ?: repository.getFallbackMovies().first()

                _homeUiState.value = _homeUiState.value.copy(
                    heroMedia = hero,
                    topMovies = topRatedList.take(10),
                    trendingMovies = trendingList,
                    popularTv = tvList,
                    nowPlaying = nowPlayingList,
                    isLoading = false,
                    error = null
                )
            } catch (_: Throwable) {
                val fallback = repository.getFallbackMovies()
                _homeUiState.value = _homeUiState.value.copy(
                    heroMedia = fallback.firstOrNull(),
                    topMovies = fallback,
                    trendingMovies = fallback,
                    popularTv = fallback,
                    nowPlaying = fallback,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun toggleLanguage() {
        val newLang = if (_currentLanguage.value == "en-US") "ar-SA" else "en-US"
        _currentLanguage.value = newLang
        loadHomeData()
        val currentDetail = _detailUiState.value.mediaDetail
        if (currentDetail != null) {
            val isTv = currentDetail.numberOfSeasons != null
            loadDetail(currentDetail.id, isTv)
        }
    }

    fun toggleHeroMute() {
        _homeUiState.value = _homeUiState.value.copy(isMuted = !_homeUiState.value.isMuted)
    }

    fun navigateToDetail(mediaItem: MediaItem, isTv: Boolean = false) {
        _currentScreen.value = AppScreen.DETAIL
        val isTvType = isTv || mediaItem.mediaType == "tv" || mediaItem.title == null
        loadDetail(mediaItem.id, isTvType)
    }

    fun loadDetail(id: Long, isTv: Boolean) {
        viewModelScope.launch {
            _detailUiState.value = DetailUiState(isLoading = true, isFavorite = repository.isFavorite(id))
            val result = repository.getMediaDetails(id, isTv, _currentLanguage.value)
            result.onSuccess { detail ->
                _detailUiState.value = DetailUiState(
                    mediaDetail = detail,
                    isLoading = false,
                    isFavorite = repository.isFavorite(id)
                )
            }.onFailure { err ->
                val fallback = repository.getFallbackDetail(id)
                _detailUiState.value = DetailUiState(
                    mediaDetail = fallback,
                    isLoading = false,
                    isFavorite = repository.isFavorite(id)
                )
            }
        }
    }

    fun toggleFavoriteCurrent() {
        val detail = _detailUiState.value.mediaDetail ?: return
        val item = MediaItem(
            id = detail.id,
            title = detail.title,
            name = detail.name,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            releaseDate = detail.releaseDate,
            firstAirDate = detail.firstAirDate,
            voteAverage = detail.voteAverage,
            mediaType = if (detail.numberOfSeasons != null) "tv" else "movie"
        )
        repository.toggleWatchlist(item)
        _detailUiState.value = _detailUiState.value.copy(
            isFavorite = repository.isFavorite(detail.id)
        )
    }

    fun toggleFavorite(item: MediaItem) {
        repository.toggleWatchlist(item)
    }

    fun isItemFavorite(id: Long): Boolean {
        return repository.isFavorite(id)
    }

    fun navigateToSearch() {
        _currentScreen.value = AppScreen.SEARCH
    }

    fun navigateToWatchlist() {
        _currentScreen.value = AppScreen.WATCHLIST
    }

    fun navigateToHome() {
        _currentScreen.value = AppScreen.HOME
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchUiState.value = _searchUiState.value.copy(query = newQuery)
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _searchUiState.value = _searchUiState.value.copy(results = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _searchUiState.value = _searchUiState.value.copy(isSearching = true)
            val result = repository.search(newQuery, _currentLanguage.value)
            result.onSuccess { list ->
                _searchUiState.value = _searchUiState.value.copy(
                    results = list,
                    isSearching = false
                )
            }.onFailure {
                _searchUiState.value = _searchUiState.value.copy(isSearching = false)
            }
        }
    }

    fun setSearchCategory(category: SearchCategory) {
        _searchUiState.value = _searchUiState.value.copy(category = category)
    }

    fun playVideo(videoKey: String?, title: String) {
        _activeVideoKey.value = videoKey
        _activeDirectUrl.value = null
        _activeVideoTitle.value = title
    }

    fun playDirectStream(url: String, title: String) {
        _activeDirectUrl.value = url
        _activeVideoKey.value = null
        _activeVideoTitle.value = title
    }

    fun closeVideoPlayer() {
        _activeVideoKey.value = null
        _activeDirectUrl.value = null
    }

    fun openStreamScreen(item: MediaItem) {
        _currentScreen.value = AppScreen.STREAM_LINKS
        _streamLinksUiState.value = StreamLinksUiState(
            mediaItem = item,
            isLoading = true,
            activeQuery = item.displayTitle
        )
        loadDramaLinks(item.displayTitle)
    }

    fun openStreamScreenFromDetail() {
        val detail = _detailUiState.value.mediaDetail ?: return
        val item = MediaItem(
            id = detail.id,
            title = detail.title,
            name = detail.name,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            releaseDate = detail.releaseDate,
            firstAirDate = detail.firstAirDate,
            voteAverage = detail.voteAverage,
            mediaType = if (detail.numberOfSeasons != null) "tv" else "movie"
        )
        _currentScreen.value = AppScreen.STREAM_LINKS
        _streamLinksUiState.value = StreamLinksUiState(
            mediaItem = item,
            mediaDetail = detail,
            isLoading = true,
            activeQuery = detail.displayTitle
        )
        loadDramaLinks(detail.displayTitle)
    }

    fun loadDramaLinks(query: String) {
        viewModelScope.launch {
            _streamLinksUiState.value = _streamLinksUiState.value.copy(
                isLoading = true,
                error = null,
                activeQuery = query
            )

            var results = QDramaScraper.search(query)
            if (results.isEmpty()) {
                val words = query.split(Regex("[^\\p{L}\\p{Nd}]+")).filter { it.length > 2 }
                if (words.isNotEmpty()) {
                    val shortQuery = words.take(2).joinToString(" ")
                    if (shortQuery != query) {
                        results = QDramaScraper.search(shortQuery)
                    }
                }
            }
            if (results.isEmpty()) {
                results = QDramaScraper.search("مسلسل")
            }

            _streamLinksUiState.value = _streamLinksUiState.value.copy(
                links = results,
                isLoading = false,
                error = if (results.isEmpty()) "لم يتم العثور على روابط متوفرة حالياً" else null
            )
        }
    }

    fun playMediaDefault(item: MediaItem) {
        openStreamScreen(item)
    }
}
