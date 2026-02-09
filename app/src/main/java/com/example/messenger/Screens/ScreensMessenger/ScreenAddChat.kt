package com.example.messenger.Screens.ScreensMessenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.MyIconButton
import com.example.messenger.Screen
import com.example.messenger.SearchBar
import com.example.messenger.User
import com.example.messenger.ButtonBack
import kotlinx.coroutines.launch

@Composable
fun ScreenAddChat(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val searchQuery = remember { mutableStateOf("") }
    val list = remember { mutableStateOf(emptyList<User>()) }
    val isLoading = remember { mutableStateOf(false) }

    fun loadMoreFriends() {
        if (isLoading.value) return

        val currentSize = list.value.size
        val allFriendsIds = viewModel.loggedInUser.friends

        if (currentSize >= allFriendsIds.size) return

        isLoading.value = true

        scope.launch {
            try {
                val nextBatchIds = allFriendsIds.drop(currentSize).take(10)
                val newUsers = viewModel.getTenUsers(nextBatchIds)
                list.value = list.value + newUsers
            } catch (e: Exception) {
                println("Ошибка при загрузке друзей: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMoreFriends()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ButtonBack({ navController.navigate(Screen.Messenger.route) }, true)
                        SearchBar(
                    query = searchQuery.value,
                    onQueryChange = { searchQuery.value = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    val filteredList = list.value.filter {
                        it.name.contains(searchQuery.value, ignoreCase = true) &&
                                it.name != viewModel.loggedInUser.name
                    }

                    itemsIndexed(filteredList) { index, user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            AvatarImage(
                                base64String = user.localAvatarPath,
                                modifier = Modifier.size(60.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(
                                    user.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    user.city,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary // Серый цвет города
                                )
                            }
                            MyIconButton(Icons.Default.Chat) {
                                scope.launch {
                                    val chatId = viewModel.addChat(listOf(user.uid))
                                    if (chatId.isNotEmpty()) {
                                        navController.navigate(Screen.Chat.createRoute(chatId))
                                    } else {
                                        println("Ошибка при добавлении чата")
                                    }
                                }
                            }
                        }
                        if (index == filteredList.lastIndex && searchQuery.value.isEmpty()) {
                            LaunchedEffect(list.value.size) {
                                loadMoreFriends()
                            }
                        }
                    }
                    if (isLoading.value) {
                        item {
                            Text(
                                "Загрузка...",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                MyIconButton(
                    Icons.Default.Groups,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .padding(bottom = 50.dp)
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                ) {
                    navController.navigate(Screen.AddGroup.route)
                }
            }
        }
    }
}