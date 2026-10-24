package com.example.moseproject.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moseproject.navigation.AppNavigation
import com.example.moseproject.ui.theme.MoSeProjectTheme
import com.example.moseproject.ui.view.components.CinemaNavigationAnimation
import com.example.moseproject.CineVerseApplication
import com.example.moseproject.data.auth.AuthContainer
import com.example.moseproject.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.factory((application as CineVerseApplication).authRepository, AuthContainer.passwordPolicy)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoSeProjectTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CinemaNavigationAnimation {
                        AppNavigation(authViewModel)
                    }
                }
            }
        }
    }
}
