package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaItem(
    val id: Long,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null,
    val popularity: Double = 0.0
) {
    val displayTitle: String
        get() = title ?: name ?: "Untitled"

    val releaseYear: String
        get() {
            val date = releaseDate ?: firstAirDate ?: ""
            return if (date.length >= 4) date.substring(0, 4) else "2026"
        }

    val ratingPercent: Int
        get() = (voteAverage * 10).toInt().coerceIn(0, 100)

    val posterUrl: String
        get() = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w1280$backdropPath"
        else posterUrl
}

@JsonClass(generateAdapter = true)
data class TmdbPagedResponse(
    val page: Int = 1,
    val results: List<MediaItem> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_results") val totalResults: Int = 0
)

@JsonClass(generateAdapter = true)
data class Genre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class MediaDetail(
    val id: Long,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val runtime: Int? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    val genres: List<Genre> = emptyList(),
    val videos: VideoContainer? = null,
    val credits: CreditsContainer? = null,
    val similar: TmdbPagedResponse? = null
) {
    val displayTitle: String
        get() = title ?: name ?: "Untitled"

    val releaseYear: String
        get() {
            val date = releaseDate ?: firstAirDate ?: ""
            return if (date.length >= 4) date.substring(0, 4) else "2026"
        }

    val durationOrSeasons: String
        get() {
            if (runtime != null && runtime > 0) {
                return "${runtime}m"
            }
            if (numberOfSeasons != null && numberOfSeasons > 0) {
                return "$numberOfSeasons ${if (numberOfSeasons == 1) "Season" else "Seasons"}"
            }
            return "110m"
        }

    val genresText: String
        get() = if (genres.isNotEmpty()) genres.take(2).joinToString(" & ") { it.name } else "Action & Thriller"

    val ratingPercent: Int
        get() = (voteAverage * 10).toInt().coerceIn(0, 100)

    val ratingScoreFormatted: String
        get() = String.format("%.1f", voteAverage)

    val posterUrl: String
        get() = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w1280$backdropPath"
        else posterUrl

    val youtubeTrailerKey: String?
        get() {
            val vids = videos?.results ?: return null
            return vids.firstOrNull { it.site.equals("YouTube", ignoreCase = true) && it.type.equals("Trailer", ignoreCase = true) }?.key
                ?: vids.firstOrNull { it.site.equals("YouTube", ignoreCase = true) }?.key
        }
}

@JsonClass(generateAdapter = true)
data class VideoContainer(
    val results: List<VideoItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    val id: String,
    val key: String,
    val name: String = "",
    val site: String = "YouTube",
    val type: String = "Trailer"
)

@JsonClass(generateAdapter = true)
data class CreditsContainer(
    val cast: List<CastItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CastItem(
    val id: Long,
    val name: String,
    val character: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null
) {
    val profileUrl: String
        get() = if (!profilePath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w185$profilePath" else ""
}
