package com.example.messenger.Screens.ScrreensFriends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.SearchBar
import com.example.messenger.User

@Composable
fun FindFreinds(navController: NavController, viewModel: MainViewModel) {
    val searchQuery = remember { mutableStateOf("") }
    val list = remember { mutableStateOf(emptyList<User>()) }

    LaunchedEffect(Unit) {
        viewModel.getRandomUsers {it->
            list.value = it
        }
    }
    Column(
        modifier = Modifier.padding(10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchBar(
            query = searchQuery.value,
            onQueryChange = { searchQuery.value = it }
        )
        list.value.forEach { user ->
            if (user.name.contains(searchQuery.value, ignoreCase = true)&&user.name!= viewModel.loggedInUser.name) {
                Row {
                    Box(
                        modifier = Modifier
                            .size(100.dp) //
                            .background(Color.Red, shape = CircleShape)
                    )
                    Column {
                        Text(user.name)
                        Text(user.location)
                    }
                    Column {
                        MyButton("Add Friend") { viewModel.addFriend(viewModel.loggedInUser!!.uid,user.uid)}
                        MyButton("Add Chat") { viewModel.addChat(user.uid)}
                    }
                }
            } else { }
        }
    }
}