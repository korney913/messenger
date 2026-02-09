package com.example.messenger.Screens.ScrreensFriends

import UserFriends
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.BotButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@Composable
fun ScreenFriends(navController: NavController, mainViewModel: MainViewModel) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Your Friends", "Find Friends")
    Scaffold(
        bottomBar = { BotButton(navController = navController) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (selectedTabIndex) {
                    0 -> UserFriends(navController, mainViewModel)
                    1 -> FindFriends(navController, mainViewModel)
                }
            }
        }
    }
}
