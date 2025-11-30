package com.example.messenger.Screens.ScreensMessenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.messenger.ChatViewModel
import com.example.messenger.MyButton
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.messenger.AvatarImage
import com.example.messenger.Screen

@Composable
fun ScreenAddChat(navController: NavController, chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    val listOfChats = chatViewModel.chats
    val listOfUsers = chatViewModel.users
    val listState = rememberLazyListState(listOfUsers.size)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MyButton("AddGroup") {
            navController.navigate(Screen.AddGroup.route)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
        ) {
            items(listOfUsers.values.toList()) { user ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable(onClick = {
                            //chatViewModel.openedChat = chat //попытаться убрать
                            //navController.navigate(Screen.Chat.createRoute(chat.chatId))
                        }),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarImage(
                        context,
                        user.localAvatarPath,
                        Modifier.size(50.dp)
                    )
                    if (user.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(10.dp) //
                                .background(Color.Green, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}