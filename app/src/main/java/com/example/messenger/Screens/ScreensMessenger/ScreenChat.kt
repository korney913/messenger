package com.example.messenger.Screens.ScreensMessenger

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.ChatViewModel
import com.example.messenger.Message
import com.example.messenger.Screen
import com.example.messenger.ButtonBack
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.messenger.AppState
import com.example.messenger.AvatarImage
import com.example.messenger.MessageStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import com.example.messenger.Photo
import com.example.messenger.saveImageToInternalStorage
import java.util.UUID
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messenger.MyIconButton
import com.example.messenger.MyTextField2
import com.example.messenger.R
import com.example.messenger.RemoveButton
import com.example.messenger.ZoomableImage
import com.example.messenger.dateConvertor
import com.example.messenger.formatLastSeen
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ScreenChat(navController: NavController, chatViewModel: ChatViewModel, chatId: String){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val chat = chatViewModel.chats.find { it.chatId == chatId }
    chatViewModel.openedChat = chat
    val listMessage = chat?.listMessage ?: emptyList()
    val photo = chatViewModel.photo
    val listState = rememberLazyListState(listMessage.size)
    var otherId: String? = ""
    val text = chatViewModel.text
    val boxWidth = remember { mutableStateOf(0.dp) }
    val boxHeight = remember { mutableStateOf(0.dp) }
    val density = context.resources.displayMetrics.density
    var isGroup = false
    var isOnline = false
    var lastSeen = ""
    LaunchedEffect(Unit){
        AppState.currentChatId = chatId
    }
    DisposableEffect(Unit) {
        onDispose {
            AppState.currentChatId = null
        }
    }
    LaunchedEffect(photo.value) {
        if (listMessage.isNotEmpty()) {
            val lastIndex= listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (photo.value==""&&lastIndex!=listMessage.size-1) listState.animateScrollToItem(listState.firstVisibleItemIndex -1, listState.firstVisibleItemScrollOffset)
            else listState.animateScrollToItem(listState.firstVisibleItemIndex + 1, listState.firstVisibleItemScrollOffset)
        }
    }
    LaunchedEffect(listMessage.size) {
        if (listMessage.isNotEmpty()) {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val offset = lastVisibleItem?.size?.toFloat() ?: 0f
            listState.animateScrollBy(
                value = offset,
                animationSpec = tween(durationMillis = 200)
            )
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            chatViewModel.updatePhoto(UUID.randomUUID().toString())
            saveImageToInternalStorage(context, uri, photo.value)
        }
        else {
            Log.d("Gallery", "Пользователь отменил выбор")
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .background(MaterialTheme.colorScheme.background)) {
        if (chat != null) {
            var chatTitle = "Loading..."
            var chatAvatar = ""
            isGroup = !chat.chatName.isNullOrBlank()
            if (isGroup) {
                chatTitle = chat.chatName!!
                chatAvatar = chat.chatPhoto ?: ""
            } else {
                val myId = chatViewModel.loggedInUser?.uid
                otherId = chat.participants.firstOrNull { it != myId }
                if (otherId != null) {
                    val user = chatViewModel.users[otherId]
                    chatTitle = user?.name ?: "User"
                    chatAvatar = user?.localAvatarPath ?: ""
                    isOnline = user?.isOnline ?: false
                    lastSeen = user?.lastSeen ?: ""
                } else {
                    chatTitle = "Saved Messages"
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ButtonBack({ navController.navigate(Screen.Messenger.route) }, true)
                Spacer(Modifier.width(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    AvatarImage(chatAvatar, Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            chatTitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatLastSeen(isOnline, lastSeen),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.fillMaxSize()
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        boxWidth.value = (coordinates.size.width / density).dp
                        boxHeight.value = (coordinates.size.height / density).dp
                    }) {
                Image(
                    painter = painterResource(id = R.drawable.chat_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = listState,
                ) {
                    if (listMessage.isNotEmpty()) {
                        itemsIndexed(listMessage) { index, message ->
                            if (((index > 0&&listMessage[index - 1].dateOfSend.substringBefore(" ") != message.dateOfSend.substringBefore(" "))||index == 0)&&message.dateOfSend!="") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x55000000),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(all = 5.dp)
                                    ) {
                                        Text(
                                            dateConvertor(message.dateOfSend),
                                            color = Color.White, // Белый текст на темном фоне даты
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            val isOwner = message.senderUid == chatViewModel.loggedInUser!!.uid
                            Row(
                                modifier = Modifier
                                    .padding(all = 15.dp)
                                    .fillMaxSize(),
                                horizontalArrangement = if (isOwner) Arrangement.End else Arrangement.Start,
                            ) {
                                if(!isOwner&&isGroup) AvatarImage( chatViewModel.users[message.senderUid]!!.localAvatarPath, Modifier.size(50.dp))
                                MessageBox(
                                    message,
                                    isOwner,
                                    chatViewModel,
                                    boxWidth.value,
                                    boxHeight.value,
                                    isGroup
                                )
                            }
                        }
                    }
                }
            }
            if (photo.value != "") {
                Photo(context, photo.value, Modifier.size(100.dp).background(Color.Transparent)) {
                    RemoveButton { photo.value = "" }
                }
            }
            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { // Фон панели ввода
                MyIconButton(Icons.Default.Add) {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                MyTextField2(text = text.value, hint = "message", Modifier.weight(1f)) {
                    chatViewModel.updateText(it)
                }
                MyIconButton(Icons.Filled.Send, enabled = photo.value != "" || text.value != "") {
                    scope.launch {
                        chatViewModel.sendMessage(context, text.value, photo.value)
                        text.value = ""
                        chatViewModel.updatePhoto("")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBox(message: Message, isOwner: Boolean, chatViewModel: ChatViewModel, boxWidth: Dp, boxHeight: Dp, isGroup: Boolean){
    val context = LocalContext.current
    val zoomImage = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message.messageId) {
        if (message.senderUid != chatViewModel.loggedInUser?.uid && message.status != MessageStatus.READ) {
            chatViewModel.readMessage(message)
        }
    }
    Box(modifier = Modifier
        .widthIn(max = boxWidth - 90.dp)
        .wrapContentWidth()) {
        Column(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = if (isOwner) Color(0xFF3390EC) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                if (isGroup) Text(chatViewModel.users[message.senderUid]!!.name, color = Color(0xFF0088CC), style = MaterialTheme.typography.bodyLarge) // Синий цвет имени
                if (message.photoName != "" && message.photoName != null) {
                    Box(modifier = Modifier.clickable(onClick = {
                        zoomImage.value = File(context.filesDir, message.photoName).absolutePath
                    })) {
                        Photo(
                            context,
                            message.photoName,
                            Modifier,
                            boxWidth - 100.dp,
                            boxHeight / 3 * 2,
                            message.messageText
                        )
                    }
                }
                val inlineContentId = "status_space"
                val annotatedString = buildAnnotatedString {
                    append(message.messageText)
                    appendInlineContent(inlineContentId, "[status]")
                }

                val inlineContent = mapOf(
                    inlineContentId to InlineTextContent(
                        // Задаем ширину и высоту места под статус
                        Placeholder(
                            width = (((if (message.senderUid==chatViewModel.loggedInUser?.uid) 5.6 else 3.3)
                                    * MaterialTheme.typography.bodyMedium.fontSize.value)).sp,
                            height = 0.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        // пустое место под статус
                    }
                )
                if (message.messageText != "") Text(
                    annotatedString,
                    inlineContent = inlineContent,
                    modifier = Modifier.padding(8.dp),
                    color =  MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(all = 5.dp)
                .background(
                    color = if(message.messageText=="") Color(0x44000000) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message.dateOfSend.substringAfter(" ").dropLast(3),
                color =  if(message.messageText=="") Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(5.dp))
            if(message.senderUid==chatViewModel.loggedInUser?.uid) {
                MessageStatus.MessageStatusIcon(message.status,
                    if(message.messageText=="") Color.White else MaterialTheme.colorScheme.onSurface,
                    Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                )
                Spacer(Modifier.width(5.dp))
            }
        }
    }
    if (zoomImage.value != null) {
        ZoomableImage(
            path = zoomImage.value!!,
            onDismiss = { zoomImage.value = null }
        )
    }
}