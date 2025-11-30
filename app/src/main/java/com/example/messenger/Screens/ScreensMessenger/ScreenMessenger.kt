package com.example.messenger.Screens.ScreensMessenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.AppState
import com.example.messenger.AvatarImage
import com.example.messenger.Chat
import com.example.messenger.ChatViewModel
import com.example.messenger.MessageStatus
import com.example.messenger.Screen
import com.example.messenger.SearchBar
import com.example.messenger.botButton
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.messenger.MyIconButton
import com.example.messenger.dateConvertor

@Composable
fun ScreenMessenger(navController: NavController, chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val listOfChats = chatViewModel.chats
    val listOfUsers = chatViewModel.users
    val currentUserId = chatViewModel.loggedInUser?.uid ?: ""
    val listState = rememberLazyListState(listOfChats.size)
    LaunchedEffect(Unit) {
        AppState.isInMessengerScreen = true
        chatViewModel.updatePhoto("")
        chatViewModel.updateText("")
    }
    DisposableEffect(Unit) {
        onDispose {
            AppState.isInMessengerScreen = false
        }
    }
    Scaffold(
        bottomBar = {
            botButton(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = searchQuery.value,
                onQueryChange = { searchQuery.value = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = listState,
                ) {
                    items(listOfChats) { chat ->
                        var chatTitle = ""
                        var chatAvatarPath: String? = null
                        var isGroup = false
                        var isOnline = false
                        if (!chat.chatName.isNullOrBlank()) {
                            isGroup = true
                            chatTitle = chat.chatName
                            chatAvatarPath = chat.chatPhoto
                        } else {
                            val otherParticipants = chat.participants.filter { it != currentUserId }

                            if (otherParticipants.isEmpty()) {
                                chatTitle = "Saved Messages"
                            } else {
                                val otherUserId = otherParticipants.first()
                                val otherUser = listOfUsers[otherUserId]
                                if (otherUser != null) {
                                    chatTitle = otherUser.name
                                    chatAvatarPath = otherUser.localAvatarPath
                                    isOnline = otherUser.isOnline
                                } else {
                                    chatTitle = "User..."
                                }
                            }
                        }
                        if (chatTitle.contains(searchQuery.value, ignoreCase = true)) {
                            var numberOfUnreadMessages = 0
                            if (chat.listMessage.isNotEmpty()) {
                                val lastMsg = chat.listMessage.last()
                                if (lastMsg.status != MessageStatus.READ && lastMsg.senderUid != currentUserId) {
                                    numberOfUnreadMessages = unreadMessages(chat)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        chatViewModel.openedChat = chat
                                        navController.navigate(Screen.Chat.createRoute(chat.chatId))
                                        if (numberOfUnreadMessages != 0) {
                                            chat.listMessage.takeLast(numberOfUnreadMessages)
                                                .forEach { item ->
                                                    chatViewModel.readMessage(item)
                                                }
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    AvatarImage(
                                        context,
                                        chatAvatarPath,
                                        Modifier.size(60.dp)
                                    )
                                    if (!isGroup && isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color.Green, shape = CircleShape)
                                                .align(Alignment.BottomEnd)
                                                .padding(2.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = chatTitle,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (chat.listMessage.isNotEmpty()) {
                                        Row {
                                            val lastMsg = chat.listMessage.last()
                                            val previewText =
                                                if (isGroup) {
                                                    if (lastMsg.senderUid != currentUserId) "${listOfUsers[lastMsg.senderUid]?.name ?: ""}: ${lastMsg.messageText}"
                                                    else "You: ${lastMsg.messageText}"
                                                } else {
                                                    lastMsg.messageText
                                                }
                                            Text(
                                                text = previewText,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (lastMsg.senderUid==currentUserId) {
                                                MessageStatus.MessageStatusIcon(lastMsg.status, MaterialTheme.colorScheme.secondary)
                                                Spacer(Modifier.width(5.dp))
                                            }
                                            Text(
                                                dateConvertor(lastMsg.dateOfSend),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 18.sp
                                            )                                        }
                                    }
                                }

                                if (numberOfUnreadMessages > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$numberOfUnreadMessages",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Кнопка создания чата
                MyIconButton(
                    Icons.Default.Create,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                ) {
                    navController.navigate(Screen.AddChat.route)
                }
            }
        }
    }
}

fun unreadMessages(chat: Chat):Int{
    var n = 1
    val N = chat.listMessage.size
    try {
        while (chat.listMessage[N-n-1].status!= MessageStatus.READ)
            n++
    }
    catch (e: Exception){}
    return n
}