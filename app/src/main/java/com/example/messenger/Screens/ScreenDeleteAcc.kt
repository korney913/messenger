package com.example.messenger.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.MyTextField
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.ButtonBack
import com.example.messenger.DataBase.FireBase
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

@Composable
fun DeleteAcc(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val email = remember {mutableStateOf("")}
    val password = remember {mutableStateOf("")}
    val errorMessage = remember { mutableStateOf("") }
    val db = FireBase()
    Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ButtonBack(onClick = {navController.navigate(Screen.Settings.route)})
            Text(
                stringResource(R.string.btn_delete_acc),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).padding(end = 48.dp),
            )
        }
        MyTextField(email.value,stringResource(R.string.hint_email)){ email.value = it }
        MyTextField(password.value,stringResource(R.string.hint_password)){ password.value = it }
        MyButton(stringResource(R.string.btn_delete_acc), modifier = Modifier.fillMaxWidth()) {
            scope.launch {
                val result = db.deleteAccount(email.value, password.value)

                result.onSuccess {
                    navController.navigate(Screen.LogIn.route) { popUpTo(0) }
                    viewModel.clearDatabase()
                }.onFailure { error ->
                    errorMessage.value = if (error is FirebaseAuthInvalidUserException) {
                        context.getString(R.string.wrong_password_or_email)
                    } else {
                        "${context.getString(R.string.delete_account_failed)} ${error.localizedMessage}"
                    }
                }
            }
        }
        Text(errorMessage.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}