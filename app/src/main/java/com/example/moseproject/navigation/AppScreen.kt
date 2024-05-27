package com.example.moseproject.navigation

sealed class AppScreen (val route:String){

    object HomeScreen: AppScreen("home_screen")
    object FilmScreen: AppScreen("film_screen")
    object SerieScreen: AppScreen("serie_screen")
    object PeopleScreen : AppScreen("people_screen")
    object SearhScreenFilm: AppScreen("search_screen_film")
    object SearhScreenSerie: AppScreen("search_screen_serie")
    object SearchPerson : AppScreen("search_person")
    object FilmID : AppScreen("film_id")
    object SerieID : AppScreen("serie_id")
    object PersonID : AppScreen("person_id")
    object AllDay : AppScreen("allday_screen")
    object AllWeek : AppScreen("allweek_screen")
    object UpcomingFilm : AppScreen("upcomingfilm_screen")
    object TopFilm : AppScreen("topfilm_screen")
    object SeriesToday : AppScreen("seriestoday_screen")
    object SeriesTrending : AppScreen("seriestrending_screen")
    object SeriesTop : AppScreen("seriestop_screen")
}