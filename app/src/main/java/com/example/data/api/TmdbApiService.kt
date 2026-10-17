package com.example.data.api

import com.example.data.model.MediaDetail
import com.example.data.model.TmdbPagedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse

    @GET("trending/tv/{time_window}")
    suspend fun getTrendingTv(
        @Path("time_window") timeWindow: String = "week",
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar"
    ): MediaDetail

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Long,
        @Query("api_key") apiKey: String = TmdbConstants.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "videos,credits,similar"
    ): MediaDetail
}
