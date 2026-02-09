package com.example.messenger.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import com.example.messenger.ButtonForSettings
import com.example.messenger.ButtonTextSize
import com.example.messenger.MainViewModel
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.BotButton
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.messenger.DataBase.FireBase
import com.example.messenger.applyAppLocale
import com.example.messenger.setUserOnline
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val TEXT_SIZE_KEY = floatPreferencesKey("text_size")

    suspend fun saveSettings(dark: Boolean, lang: String, size: Float) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = dark
            prefs[LANGUAGE_KEY] = lang
            prefs[TEXT_SIZE_KEY] = size
        }
    }

    suspend fun loadSettings() {
        val prefs = context.dataStore.data.first()

        Settings.darkTheme.value = prefs[DARK_THEME_KEY] ?: false
        Settings.textSizeScale.value = prefs[TEXT_SIZE_KEY] ?: 1.0f

        val langTag = prefs[LANGUAGE_KEY] ?: Locale.getDefault().toLanguageTag()
        Settings.language.value = Locale.forLanguageTag(langTag)
    }
}

data object Settings {
    val darkTheme = mutableStateOf(false)
    val language = mutableStateOf(Locale.getDefault())
    val textSizeScale = mutableStateOf(1f)

    fun persist(context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope) {
        val dataStore = SettingsDataStore(context)
        scope.launch {
            dataStore.saveSettings(
                darkTheme.value,
                language.value.toLanguageTag(),
                textSizeScale.value
            )
        }
    }
}

@Composable
fun ScreenSettings(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FireBase()
    Scaffold(
        bottomBar = {
            BotButton(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(colorScheme.background)
        ) {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )

            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                ButtonForSettings(stringResource(R.string.dark_theme)) {
                    Settings.darkTheme.value = !Settings.darkTheme.value
                    Settings.persist(context, scope)
                }
                ButtonTextSize { Settings.persist(context, scope) }
                ButtonForSettings(stringResource(R.string.switch_language)) {
                    Settings.language.value =
                        if (Settings.language.value.language == "en") Locale("ru") else Locale("en")
                    Settings.persist(context, scope)
                    applyAppLocale(context, Settings.language.value)
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // Отступ перед опасными действиями
            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                ButtonForSettings(stringResource(R.string.btn_sign_out)) {
                    scope.launch {
                        setUserOnline(false)
                        viewModel.clearDatabase()
                        val result = db.signOut()
                        if (result.isSuccess) {
                            navController.navigate(Screen.LogIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
                ButtonForSettings(stringResource(R.string.btn_delete_acc)) {
                    navController.navigate(Screen.DeleteAcc.route)
                }
            }
        }
    }
}