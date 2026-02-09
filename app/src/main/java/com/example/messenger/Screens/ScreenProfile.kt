package com.example.messenger.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.R
import com.example.messenger.BotButton
import com.example.messenger.saveImageToInternalStorage


@Composable
fun ScreenProfile(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val savedAvatarPath = remember {mutableStateOf(viewModel.loggedInUser.localAvatarPath)}
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "${viewModel.loggedInUser.uid}?${System.currentTimeMillis()}"
            val path = saveImageToInternalStorage(context, uri, fileName)
            if (path != null) {
                savedAvatarPath.value = fileName
                viewModel.changeAvatar(context, fileName)
            }
        }
    }
    Scaffold(
        bottomBar = {
            BotButton(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AvatarImage(savedAvatarPath.value, Modifier
                    .size(200.dp)
                    .clickable(onClick = { launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    ) }))
            }
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text( viewModel.loggedInUser.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Birthday",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.birthday)}: ${viewModel.loggedInUser.dateOfBirth}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "City",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.city)}: ${viewModel.loggedInUser.city}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()
                    .clickable(onClick = {navController.navigate("friends")}),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Friends",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "${stringResource(R.string.friends)}: ${viewModel.loggedInUser.friends.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

