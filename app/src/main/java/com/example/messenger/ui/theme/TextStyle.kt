package com.example.messenger.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HintText(text: String) {
    Text(
        text = text,
        color = Gray,
        fontSize = 16.sp
    )
}