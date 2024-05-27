package com.example.moseproject.data

import com.example.moseproject.data.model.All
import com.example.moseproject.data.model.CreditsResult
import com.example.moseproject.data.model.FilmIdResult
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.model.KnownForResult
import com.example.moseproject.data.model.PeopleResult
import com.example.moseproject.data.model.PersonResult
import com.example.moseproject.data.model.SearchPersonResult
import com.example.moseproject.data.model.SerieIDResult
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.model.VideoResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @GET("movie/popular?language=es-ES&page=1&region=es")
    suspend fun listPopularMovies(
        @Query("api_key") apiKey :String
    ): FilmResult

    @GET("movie/upcoming?language=es&region=es")
    suspend fun listUpcomingFilms(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int
    ): FilmResult


    @GET("movie/top_rated?language=es")
    suspend fun listTopRatedFilms(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int
    ): FilmResult


    @GET("trending/tv/day?language=es-ES")
    suspend fun listTrendigSeries(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int
    ): SerieResult

    @GET("tv/airing_today?language=es-ES")
    suspend fun listTodaySeries(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int
    ): SerieResult

    @GET("tv/top_rated?language=es-ES")
    suspend fun listTopRatedSeries(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int
    ): SerieResult

    @GET("search/movie?&include_adult=false&language=es-ES&page=1&region=es")
    suspend fun searchFilm(
        @Query("query") title : String,
        @Query("api_key") apiKey: String
    ): FilmResult

    @GET("search/tv?&include_adult=false&language=es-ES&page=1&region=es")
    suspend fun searchSerie(
        @Query("query") title : String,
        @Query("api_key") apiKey: String
    ): SerieResult

    @GET("trending/all/week?language=es-ES")
    suspend fun listTendringAllWeek(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): All

    @GET("trending/all/day?language=es-ES")
    suspend fun listTendringAllDay(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): All

    @GET("movie/{movie_id}?language=es-ES")
    suspend fun SearchByIdFilm(
        @Path("movie_id") id :String,
        @Query("api_key") apiKey :String

    ): FilmIdResult

    @GET("tv/{series_id}?language=es-ES")
    suspend fun SearchByIdSerie(
        @Path("series_id") id :String,
        @Query("api_key") apiKey :String
    ): SerieIDResult

    @GET("movie/{movie_id}/credits?language=es-ES")
    suspend fun CastFilm(
        @Path("movie_id") id :String,
        @Query("api_key") apiKey :String
    ): CreditsResult

    @GET("tv/{series_id}/credits?language=es-ES")
    suspend fun CastSerie(
        @Path("series_id") id :String,
        @Query("api_key") apiKey :String
    ): CreditsResult

    @GET("movie/{movie_id}/videos?language=es-ES")
    suspend fun VideoMovie(
        @Path("movie_id") id :String,
        @Query("api_key") apiKey: String
    ): VideoResult

    @GET("tv/{series_id}/videos?language=es-ES")
    suspend fun VideoSerie(
        @Path("series_id") id :String,
        @Query("api_key") apiKey: String
    ): VideoResult

    @GET("person/popular?language=es-ES")
    suspend fun listPeople(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): PeopleResult

    @GET("person/{person_id}?language=es-ES")
    suspend fun searchPerson(
        @Path("person_id") id :String,
        @Query("api_key") apiKey: String
    ): PersonResult

    @GET("person/{person_id}/combined_credits?language=es-ES")
    suspend fun PersonKnowFor(
        @Path("person_id") id :String,
        @Query("api_key") apiKey: String
    ): KnownForResult

    @GET("search/person?&include_adult=false&language=es-ES&page=1")
    suspend fun searchPersonAll(
        @Query("query") title : String,
        @Query("api_key") apiKey: String
    ): SearchPersonResult

    @GET("movie/{movie_id}/recommendations?language=es-ES")
    suspend fun recommendationsFilm(
        @Path("movie_id") id :String,
        @Query("api_key") apiKey: String
    ) : FilmResult

    @GET("tv/{series_id}/recommendations?language=es-ES")
    suspend fun recommendationsSerie(
        @Path("series_id") id :String,
        @Query("api_key") apiKey: String
    ) : SerieResult
}