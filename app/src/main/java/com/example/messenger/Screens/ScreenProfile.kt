package com.example.messenger.Screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.room.util.TableInfo
import com.example.messenger.AvatarImage
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.botButton
import com.example.messenger.saveImageToInternalStorage
import java.io.File
import java.io.FileOutputStream


@Composable
fun ScreenProfile(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val savedAvatarPath = remember {mutableStateOf<String?>(viewModel.loggedInUser.localAvatarPath)}
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
        } else {
            Log.d("Gallery", "Пользователь отменил выбор")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AvatarImage(context,savedAvatarPath.value, Modifier
                    .size(200.dp)
                    .clickable(onClick = { launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    ) }))
            }
            Text( viewModel.loggedInUser.name )
            MyButton("Learn more") {
                expanded.value = true
            }
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                DropdownMenuItem(
                    text = { "" },
                    onClick = {
                    }
                )
            }

        }
    }
}

