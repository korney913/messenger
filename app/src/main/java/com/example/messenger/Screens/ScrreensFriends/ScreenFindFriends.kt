package com.example.messenger.Screens.ScrreensFriends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.MyIconButton
import com.example.messenger.Screen
import com.example.messenger.SearchBar
import com.example.messenger.User
import kotlinx.coroutines.launch

@Composable
fun FindFriends(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val searchQuery = remember { mutableStateOf("") }
    val list = remember { mutableStateOf(emptyList<User>()) }
    val isLoading = remember { mutableStateOf(false) }
    val lastUserUid = remember { mutableStateOf<String?>(null) }
    val isEndOfDatabase = remember { mutableStateOf(false) }
    fun loadMoreFriends() {
        if (isLoading.value || isEndOfDatabase.value) return
        isLoading.value = true
        scope.launch {
            try {
                val newUsers = viewModel.getTenUsers(startUid = lastUserUid.value)
                if (newUsers.isEmpty()) {
                    isEndOfDatabase.value = true
                } else {
                    lastUserUid.value = newUsers.lastOrNull()?.uid
                    val filteredNewUsers = newUsers.filter { newUser ->
                        !viewModel.loggedInUser.friends.contains(newUser.uid) &&
                                newUser.uid != viewModel.loggedInUser.uid
                    }
                    if (filteredNewUsers.isNotEmpty()) {
                        list.value = list.value + filteredNewUsers
                    }
                    if (filteredNewUsers.isEmpty() && newUsers.isNotEmpty()) {
                        isLoading.value = false
                        loadMoreFriends()
                    }
                }
            } catch (e: Exception) {
                println("Ошибка при поиске друзей: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMoreFriends()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        SearchBar(
            query = searchQuery.value,
            onQueryChange = { searchQuery.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text(user.city, style = MaterialTheme.typography.bodySmall)
                    }
                    if(!viewModel.loggedInUser.friends.contains(user.uid))
                     MyIconButton (Icons.Default.PersonAdd) {
                         scope.launch {
                             viewModel.addFriend(viewModel.loggedInUser.uid, user.uid)
                         }
                    }
                    else MyIconButton (Icons.Default.PersonRemove){
                        scope.launch {
                            viewModel.removeFriend(viewModel.loggedInUser.uid, user.uid)
                        }
                    }

                    MyIconButton (Icons.Default.Chat) {
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
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
