package com.example.messenger

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.messenger.Screens.ScreenMessenger
import com.example.messenger.Screens.ScreenProfile
import com.example.messenger.Screens.Settings
import com.example.messenger.Screens.Settings.ScreenSettings
import com.example.messenger.ui.theme.MessengerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerTheme(darkTheme= Settings.darkTheme.value) {
                //Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Navigation()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Screen1 : Screen("screen1")
    object Screen2 : Screen("screen2")
    object Screen3 : Screen("screen3")
}
@Composable
fun botButton(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        NavigationBarItem(
            icon = {  },
            label = { Text(Screen.Screen1.route, fontSize = 24.sp) },
            selected = currentRoute == Screen.Screen1.route,
            onClick = {
                navController.navigate(Screen.Screen1.route)
            }
        )
        NavigationBarItem(
            icon = {  },
            label = { Text(Screen.Screen2.route, fontSize = 24.sp) },
            selected = currentRoute == Screen.Screen2.route,
            onClick = {
                navController.navigate(Screen.Screen2.route)
            }
        )
        NavigationBarItem(
            icon = {  },
            label = { Text(Screen.Screen3.route, fontSize = 24.sp) },
            selected = currentRoute == Screen.Screen3.route,
            onClick = {
                navController.navigate(Screen.Screen3.route)
            }
        )
    }
}
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.Screen1.route
    ) {
        composable(Screen.Screen1.route) { ScreenMessenger(navController, mainViewModel) }
        composable(Screen.Screen2.route) { ScreenProfile(navController, mainViewModel) }
        composable(Screen.Screen3.route) { ScreenSettings(navController, mainViewModel) }
    }
}

class MainViewModel : ViewModel() {
    var currentLocale by mutableStateOf(Settings.language.value)
        private set

    fun toggleLanguage(context: Context) {
        currentLocale = Settings.language.value
        val config = context.resources.configuration
        Locale.setDefault(Settings.language.value)
        config.setLocale(Settings.language.value)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}


