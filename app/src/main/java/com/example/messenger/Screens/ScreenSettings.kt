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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

interface SettingsRepository {
    val themeFlow: Flow<Boolean>
    val languageFlow: Flow<String>
    val textSizeFlow: Flow<Float>

    suspend fun updateTheme(isDark: Boolean)
    suspend fun updateLanguage(langTag: String)
    suspend fun updateTextSize(size: Float)
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    private val dataStore = context.dataStore

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val TEXT_SIZE_KEY = floatPreferencesKey("text_size")

    override val themeFlow = dataStore.data.map { it[DARK_THEME_KEY] ?: false }
    override val languageFlow = dataStore.data.map { it[LANGUAGE_KEY] ?: Locale.getDefault().toLanguageTag() }
    override val textSizeFlow = dataStore.data.map { it[TEXT_SIZE_KEY] ?: 1.0f }

    override suspend fun updateTheme(isDark: Boolean) {
        dataStore.edit { it[DARK_THEME_KEY] = isDark }
    }
    override suspend fun updateLanguage(langTag: String) {
        dataStore.edit { it[LANGUAGE_KEY] = langTag }
    }
    override suspend fun updateTextSize(size: Float) {
        dataStore.edit { it[TEXT_SIZE_KEY] = size }
    }
}

@Composable
fun ScreenSettings(navController: NavController, viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
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
                    viewModel.updateTheme()
                }
                ButtonTextSize(viewModel)
                ButtonForSettings(stringResource(R.string.switch_language)) {
                    viewModel.updateLanguage(if (viewModel.currentLocale.language == "en") Locale("ru") else Locale("en") )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                ButtonForSettings(stringResource(R.string.btn_sign_out)) {
                    scope.launch {
                        val result = viewModel.signOut()
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