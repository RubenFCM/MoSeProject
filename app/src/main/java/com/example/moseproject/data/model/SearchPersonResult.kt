package com.example.moseproject.data.model

data class SearchPersonResult(
    val page: Int,
    val results: List<ResultSearchPerson>,
    val total_pages: Int,
    val total_results: Int
)

data class ResultSearchPerson(
    val adult: Boolean,
    val gender: Int,
    val id: Int,
    val known_for: List<KnownForSP>,
    val known_for_department: String,
    val name: String,
    val original_name: String,
    val popularity: Double,
    val profile_path: String
)

data class KnownForSP(
    val adult: Boolean,
    val backdrop_path: String,
    val first_air_date: String,
    val genre_ids: List<Int>,
    val id: Int,
    val media_type: String,
    val name: String,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)