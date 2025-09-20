package com.example.messenger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor =  MaterialTheme.colorScheme.secondary,//if (rButton) LightGray else
            contentColor = Color.White
        ),
        /*modifier = Modifier.fillMaxWidth()
            .height(60.dp)
            .border(
                BorderStroke(2.dp, LightGray),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)*/
    ){
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun buttonBack(onClick: () -> Unit,enabled: Boolean = true) {
    Button(//modifier = Modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        onClick = onClick,
        enabled =enabled,
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Назад"
        )
    }
}