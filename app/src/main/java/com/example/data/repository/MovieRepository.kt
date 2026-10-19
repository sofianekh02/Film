package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.ApiClient
import com.example.data.api.TmdbApiService
import com.example.data.model.MediaDetail
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MovieRepository(
    private val context: Context,
    private val apiService: TmdbApiService = ApiClient.tmdbService
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("top_movies_prefs", Context.MODE_PRIVATE)

    private val _watchlistFlow = MutableStateFlow<List<MediaItem>>(emptyList())
    val watchlistFlow: StateFlow<List<MediaItem>> = _watchlistFlow.asStateFlow()

    init {
        loadWatchlist()
    }

    suspend fun getTrendingMovies(language: String): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTrendingMovies(language = language)
                Result.success(response.results.filter { !it.posterPath.isNullOrEmpty() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getTopRatedMovies(language: String): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopRatedMovies(language = language)
                Result.success(response.results.filter { !it.posterPath.isNullOrEmpty() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getTrendingTv(language: String): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTrendingTv(language = language)
                Result.success(response.results.filter { !it.posterPath.isNullOrEmpty() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getNowPlaying(language: String): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNowPlayingMovies(language = language)
                Result.success(response.results.filter { !it.posterPath.isNullOrEmpty() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMediaDetails(id: Long, isTv: Boolean, language: String): Result<MediaDetail> =
        withContext(Dispatchers.IO) {
            try {
                val detail = if (isTv) {
                    apiService.getTvDetails(tvId = id, language = language)
                } else {
                    apiService.getMovieDetails(movieId = id, language = language)
                }
                Result.success(detail)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun search(query: String, language: String): Result<List<MediaItem>> =
        withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) return@withContext Result.success(emptyList())
                val response = apiService.searchMulti(query = query, language = language)
                Result.success(response.results.filter { !it.posterPath.isNullOrEmpty() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun isFavorite(id: Long): Boolean {
        return _watchlistFlow.value.any { it.id == id }
    }

    fun toggleWatchlist(item: MediaItem) {
        val current = _watchlistFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, item)
        }
        _watchlistFlow.value = current
        saveWatchlist(current)
    }

    private fun saveWatchlist(list: List<MediaItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title ?: item.name ?: "")
                put("posterPath", item.posterPath ?: "")
                put("backdropPath", item.backdropPath ?: "")
                put("releaseDate", item.releaseDate ?: item.firstAirDate ?: "")
                put("voteAverage", item.voteAverage)
                put("mediaType", item.mediaType ?: "movie")
                put("overview", item.overview ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_watchlist", array.toString()).apply()
    }

    private fun loadWatchlist() {
        val json = prefs.getString("saved_watchlist", null) ?: return
        try {
            val array = JSONArray(json)
            val list = mutableListOf<MediaItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MediaItem(
                        id = obj.optLong("id"),
                        title = obj.optString("title"),
                        posterPath = obj.optString("posterPath"),
                        backdropPath = obj.optString("backdropPath"),
                        releaseDate = obj.optString("releaseDate"),
                        voteAverage = obj.optDouble("voteAverage", 0.0),
                        mediaType = obj.optString("mediaType"),
                        overview = obj.optString("overview")
                    )
                )
            }
            _watchlistFlow.value = list
        } catch (_: Exception) {
        }
    }

    fun getFallbackMovies(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = 99901,
                title = "Masterplan",
                overview = "When a legendary thief sets his sights on the Mona Lisa, he recruits two young strangers to help him before revealing they are long-lost siblings... and oh yes, by the way, he is their father. Thrown into a chaotic reunion they never asked for, this new family must learn to work together — and try not to kill each other in the process.",
                posterPath = "/ujUwzNV8ZkOzZonvhhHsc9KOKDf.jpg",
                backdropPath = "/ujUwzNV8ZkOzZonvhhHsc9KOKDf.jpg",
                releaseDate = "2026-01-01",
                voteAverage = 7.0,
                mediaType = "movie"
            ),
            MediaItem(
                id = 99902,
                title = "Tenzing",
                overview = "The historic expedition of Tenzing Norgay and Sir Edmund Hillary conquering Mount Everest.",
                posterPath = "/fM5L8ZqB7H8YVw7zFq2Jm9X8e1L.jpg",
                backdropPath = "/fM5L8ZqB7H8YVw7zFq2Jm9X8e1L.jpg",
                releaseDate = "2026-03-12",
                voteAverage = 8.0,
                mediaType = "movie"
            ),
            MediaItem(
                id = 99903,
                title = "Crystal Lake",
                overview = "A terrifying deep dive into the haunted waters and tragic legends of Crystal Lake.",
                posterPath = "/bAO1q8V1d5P3e5T4g6M7r2N9k8.jpg",
                backdropPath = "/bAO1q8V1d5P3e5T4g6M7r2N9k8.jpg",
                releaseDate = "2026-04-18",
                voteAverage = 8.2,
                mediaType = "movie"
            ),
            MediaItem(
                id = 99904,
                title = "VisionQuest",
                overview = "A mind-bending journey of technology and human destiny in a futuristic metropolis.",
                posterPath = "/a7K3h1L9z4m5B2c8X6p7D1e0Q9.jpg",
                backdropPath = "/a7K3h1L9z4m5B2c8X6p7D1e0Q9.jpg",
                releaseDate = "2026-05-22",
                voteAverage = 9.0,
                mediaType = "movie"
            ),
            MediaItem(
                id = 99905,
                title = "Hope",
                overview = "In the remote village of Hope Harbor, investigators confront an unknown presence emerging from the dark forest.",
                posterPath = "/ujUwzNV8ZkOzZonvhhHsc9KOKDf.jpg",
                backdropPath = "/ujUwzNV8ZkOzZonvhhHsc9KOKDf.jpg",
                releaseDate = "2026-06-15",
                voteAverage = 7.9,
                mediaType = "movie"
            )
        )
    }

    fun getFallbackDetail(id: Long): MediaDetail {
        val base = getFallbackMovies().firstOrNull { it.id == id } ?: getFallbackMovies().first()
        return MediaDetail(
            id = base.id,
            title = base.title,
            name = base.name,
            overview = base.overview,
            runtime = 110,
            releaseDate = base.releaseDate,
            voteAverage = base.voteAverage,
            posterPath = base.posterPath,
            backdropPath = base.backdropPath,
            genres = listOf(
                com.example.data.model.Genre(1, "Action"),
                com.example.data.model.Genre(2, "Thriller")
            ),
            similar = com.example.data.model.TmdbPagedResponse(
                page = 1,
                results = getFallbackMovies().filter { it.id != base.id }
            )
        )
    }
}
