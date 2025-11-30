package com.example.messenger.Screens.ScrreensFriends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.botButton

@Composable
fun ScreenFriends(navController: NavController,mainViewModel: MainViewModel) {
    val switchScreen = remember { mutableStateOf(true) }
    Scaffold(
        bottomBar = {
            botButton(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ){
            Row(modifier = Modifier.fillMaxWidth()) {
                MyButton("Your Friends") {switchScreen.value = true }
                MyButton("Find Friends") {switchScreen.value = false }
            }
            if (switchScreen.value) UserFreinds(navController,mainViewModel)
                else FindFreinds(navController,mainViewModel)
        }
    }
}
