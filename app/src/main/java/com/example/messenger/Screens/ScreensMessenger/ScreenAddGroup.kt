package com.example.messenger.Screens.ScreensMessenger

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.messenger.MyButton
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.MyTextField
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.SearchBar
import com.example.messenger.User
import com.example.messenger.ButtonBack
import kotlinx.coroutines.launch
import kotlin.collections.plus

@Composable
fun ScreenAddGroup(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val groupName = remember { mutableStateOf("") }
    val listOfParticipants = remember { mutableStateOf(emptyList<String>()) }
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
            MyTextField(groupName.value, stringResource(R.string.group_name)) { groupName.value = it }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {

                val filteredList = list.value.filter {
                    it.name.contains(searchQuery.value, ignoreCase = true) &&
                            it.name != viewModel.loggedInUser.name
                }
                itemsIndexed(filteredList) { index, user ->
                    val isSelected = listOfParticipants.value.contains(user.uid)
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
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    listOfParticipants.value = listOfParticipants.value + user.uid
                                } else {
                                    listOfParticipants.value = listOfParticipants.value - user.uid
                                }
                            }
                        )
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
            Spacer(modifier = Modifier.height(4.dp))
            MyButton("Create group", modifier = Modifier.fillMaxWidth(),
                enabled = groupName.value.isNotEmpty() && listOfParticipants.value.isNotEmpty()) {
                scope.launch {
                    val id = viewModel.addChat(
                        listOfParticipants.value,
                        groupName.value,
                        null,
                        viewModel.loggedInUser.uid
                    )
                    if (id.isNotEmpty()) {
                        navController.navigate(Screen.Chat.createRoute(id))
                    }
                }
            }
        }
    }
}
