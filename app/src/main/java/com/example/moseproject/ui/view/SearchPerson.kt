package com.example.moseproject.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.moseproject.ui.view.components.CinemaSearch

@Composable
fun SearchPerson(navController: NavController) = CinemaSearch(navController, "person")
