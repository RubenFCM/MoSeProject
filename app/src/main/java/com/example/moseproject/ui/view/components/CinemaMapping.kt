package com.example.moseproject.ui.view.components

import com.example.moseproject.data.model.*

fun Result.toCinemaItem() = CinemaItem(id, title, poster_path, "movie", release_date, vote_average, backdrop_path, overview)
fun ResultSerie.toCinemaItem() = CinemaItem(id, name, poster_path, "tv", first_air_date, vote_average, backdrop_path, overview)
fun ResultAll.toCinemaItem() = CinemaItem(id, if (media_type == "movie") title else name, poster_path, media_type,
    if (media_type == "movie") release_date else first_air_date, vote_average, backdrop_path, overview)
fun ResultPeople.toCinemaItem() = CinemaItem(id, name, profile_path, "person")
fun CastResult.toCinemaItem() = CinemaItem(id, if (media_type == "movie") title else name, poster_path, media_type,
    if (media_type == "movie") release_date else first_air_date, vote_average, backdrop_path, overview)
