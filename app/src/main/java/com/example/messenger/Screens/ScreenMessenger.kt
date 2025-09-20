package com.example.messenger.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.botButton

@Composable
fun ScreenMessenger(navController: NavController, viewModel: MainViewModel) {
    Scaffold(
        bottomBar = {
            botButton(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val list = listOf(1, 2, 3, 4, 5)
            list.forEach { n ->
                Row {
                    Box(
                        modifier = Modifier
                            .size(100.dp) //
                            .background(Color.Red, shape = CircleShape)
                    )
                    Column {
                        Text("Пользователь $n")
                        Text("Последние сообщение")
                    }
                }
            }
        }
    }
}