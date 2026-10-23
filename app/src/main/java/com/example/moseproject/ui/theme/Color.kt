package com.example.moseproject.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.moseproject.R

// Midnight Teal is defined once in res/values/colors.xml, shared with the launch theme.
val CinemaBackground: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_background)

val CinemaSurface: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_surface)

val CinemaSelection: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_selection)

val CinemaAccent: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_accent)

val CinemaText: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_text)

val CinemaMuted: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_text_secondary)

val CinemaNavigationBackground: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_navigation)

val CinemaRating: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_rating)

val CinemaLogoBackground: Color
    @Composable @ReadOnlyComposable
    get() = colorResource(R.color.cinema_logo_background)

val CinemaElevated: Color
    @Composable @ReadOnlyComposable
    get() = CinemaSurface

val CinemaOutline: Color
    @Composable @ReadOnlyComposable
    get() = CinemaSelection

val CinemaNavigationSelection: Color
    @Composable @ReadOnlyComposable
    get() = CinemaSelection

val CinemaNavigationActive: Color
    @Composable @ReadOnlyComposable
    get() = CinemaAccent

val CinemaNavigationInactive: Color
    @Composable @ReadOnlyComposable
    get() = CinemaMuted
