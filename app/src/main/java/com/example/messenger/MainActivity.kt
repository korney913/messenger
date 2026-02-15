package com.example.messenger

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messenger.Screens.ScreensMessenger.ScreenChat
import com.example.messenger.Screens.ScrreensFriends.ScreenFriends
import com.example.messenger.Screens.SignIn
import com.example.messenger.Screens.ScreensMessenger.ScreenMessenger
import com.example.messenger.Screens.ScreenProfile
import com.example.messenger.Screens.ScreenSettings
import com.example.messenger.Screens.SignUp
import com.example.messenger.ui.theme.MessengerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.String
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.navigation
import com.example.messenger.Screens.DeleteAcc
import com.example.messenger.Screens.ScreensMessenger.ScreenAddChat
import com.example.messenger.Screens.ScreensMessenger.ScreenAddGroup
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> setUserOnline(true)
                    Lifecycle.Event.ON_STOP -> setUserOnline(false)
                    else -> {}
                }
            }
        )
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.factory)
            LaunchedEffect(mainViewModel.currentLocale) {
                applyAppLocale(this@MainActivity, mainViewModel.currentLocale)
            }
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(mainViewModel.currentLocale)
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                MessengerTheme(
                    darkTheme = mainViewModel.isDarkTheme,
                    multiplier = mainViewModel.textSizeScale
                ) {
                    Navigation(mainViewModel)
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        setUserOnline(false)
    }
}

fun applyAppLocale(context: Context, locale: Locale) {
    Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    context.applicationContext.resources.updateConfiguration(config, resources.displayMetrics)
}

sealed class Screen(val route: String) {
    object Messenger : Screen("Messenger")
    object Profile : Screen("Profile}")
    object Friends : Screen("Friends")
    object Settings : Screen("Settings")
    object SignUp : Screen("SignUp")
    object LogIn : Screen("LogIn")
    object DeleteAcc : Screen("DeleteAcc")
    object AddChat : Screen("AddChat")
    object AddGroup : Screen("AddGroup")
    object Chat : Screen("Chat/{chatId}") {
        fun createRoute(chatId: String) = "Chat/$chatId"
    }
}
@Composable
fun BotButton(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding: Dp = with(LocalDensity.current) { navigationBars.calculateBottomPadding() }
    NavigationBar(containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.height(50.dp + bottomPadding)) {
        NavigationBarItem(
            modifier = Modifier.padding(start = 15.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(30.dp)
                )
            },
            selected = currentRoute == Screen.Messenger.route,
            onClick = { navController.navigate(Screen.Messenger.route) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(30.dp)
                )
            },
            selected = currentRoute == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Friends",
                    modifier = Modifier.size(30.dp)
                )
            },
            selected = currentRoute == Screen.Friends.route,
            onClick = { navController.navigate(Screen.Friends.route) }
        )
        NavigationBarItem(
            modifier = Modifier.padding(end = 15.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(30.dp)
                )
            },
            selected = currentRoute == Screen.Settings.route,
            onClick = { navController.navigate(Screen.Settings.route) }
        )
    }
}


@Composable
fun Navigation(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val user = FirebaseAuth.getInstance().currentUser
    val userIsLoggedIn = user?.uid != null
    val startDestination = if (userIsLoggedIn)
        "chatGraph"
    else
        Screen.LogIn.route
    RequestNotificationPermission()
    NavHost(
        navController = navController,
        startDestination =  startDestination
    ) {
        composable(Screen.Profile.route) { ScreenProfile(navController, mainViewModel) }
        composable(Screen.Friends.route) { ScreenFriends(navController, mainViewModel) }
        composable(Screen.Settings.route) { ScreenSettings(navController, mainViewModel) }
        composable(Screen.LogIn.route) { SignIn(navController, mainViewModel) }
        composable(Screen.DeleteAcc.route) { DeleteAcc(navController, mainViewModel) }
        composable(Screen.SignUp.route) { SignUp(navController, mainViewModel) }
        navigation(
            startDestination = Screen.Messenger.route,
            route = "chatGraph"
        ) {
            composable(Screen.Messenger.route) { backStackEntry ->
                val chatGraphBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("chatGraph")
                }
                val chatViewModel: ChatViewModel = viewModel(
                    viewModelStoreOwner  = chatGraphBackStackEntry,
                    factory = ChatViewModel.factory
                )
                ScreenMessenger(navController, chatViewModel)
            }
            composable(Screen.AddChat.route) { backStackEntry ->
                ScreenAddChat(navController, mainViewModel)
            }
            composable(Screen.AddGroup.route) { backStackEntry ->
                ScreenAddGroup(navController, mainViewModel)
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatGraphBackStackEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("chatGraph")
                }
                val chatViewModel: ChatViewModel = viewModel(
                    viewModelStoreOwner  = chatGraphBackStackEntry,
                    factory = ChatViewModel.factory
                )
                val chatId = backStackEntry.arguments?.getString("chatId")!!
                ScreenChat(navController, chatViewModel, chatId)
            }
        }
    }
}



fun setUserOnline(isOnline: Boolean) {
    val user = FirebaseAuth.getInstance().currentUser
    user?.let {
        FirebaseFirestore.getInstance().collection("Users")
            .document(it.uid)
            .update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to System.currentTimeMillis()
                )
            )
            .addOnFailureListener { e ->
                Log.e("OnlineStatus", "Error updating status", e)
            }
    }
}




