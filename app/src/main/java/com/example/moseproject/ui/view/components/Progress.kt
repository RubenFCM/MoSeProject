package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Progress(){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        CircularProgressIndicator(
            modifier = Modifier.align(alignment = Alignment.Center),
            color = Color.White
        )
    }
}