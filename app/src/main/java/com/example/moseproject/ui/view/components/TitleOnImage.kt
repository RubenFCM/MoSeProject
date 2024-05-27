package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleOnImage(title:String){
    Text(
        text = title,
        color = Color.White,
        modifier = Modifier
            .absoluteOffset(x = 4.dp, y = 140.dp)
            .width(210.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}