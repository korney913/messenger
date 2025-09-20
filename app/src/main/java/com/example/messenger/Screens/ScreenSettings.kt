package com.example.messenger.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.botButton
import java.util.Locale

object Settings {
    val darkTheme = mutableStateOf(false)
    val language = mutableStateOf(Locale.getDefault())

    @Composable
    fun ScreenSettings(navController: NavController, viewModel: MainViewModel) {
        val context = LocalContext.current
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
                MyButton(stringResource(R.string.dark_theme)) {
                    darkTheme.value = !darkTheme.value
                }
                MyButton(stringResource(R.string.switch_language)) {
                    language.value = if (language.value == Locale("en")) Locale("ru") else Locale("en")
                    viewModel.toggleLanguage(context)
                    navController.navigate(Screen.Screen3.route) { //переход на тот же экран, чтобы обновить текст кнопок
                        popUpTo(Screen.Screen3.route) { inclusive = true }
                        launchSingleTop = true   //чтобы навигация не создавала новый экран, если он уже открыт.
                    }
                }
            }
        }
    }
}
