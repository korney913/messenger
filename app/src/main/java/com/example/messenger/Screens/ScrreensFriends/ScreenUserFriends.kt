import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.MyIconButton
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.SearchBar
import com.example.messenger.User
import kotlinx.coroutines.launch

@Composable
fun UserFriends(navController: NavController, viewModel: MainViewModel) {
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