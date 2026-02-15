package com.example.messenger.Screens

import androidx.compose.foundation.background
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
import com.example.messenger.DataBase.FireBase
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.MyTextField
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.User
import com.example.messenger.ButtonBack
import kotlinx.coroutines.launch

@Composable
fun SignUp(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val dateOfBirth = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()
        .background(color = colorScheme.background)
        .padding(start = 30.dp, end = 30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ButtonBack(onClick = {navController.navigate(Screen.LogIn.route)})
            Text(
                stringResource(R.string.btn_signup2),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).padding(end = 48.dp),
                )
        }
        MyTextField(email.value,stringResource(R.string.hint_email)){ email.value = it }
        MyTextField(password.value,stringResource(R.string.hint_password)){ password.value = it }
        MyTextField(name.value,stringResource(R.string.hint_name)){ name.value = it }
        MyTextField(dateOfBirth.value, stringResource(R.string.birthday)) { dateOfBirth.value = it }
        MyTextField(location.value,stringResource(R.string.hint_location)){ location.value = it }
        MyButton(stringResource(R.string.btn_signup2), modifier = Modifier.fillMaxWidth()) {
            scope.launch {
                val result = viewModel.signUp(email.value, password.value,
                    name.value, dateOfBirth.value, location.value)
                if (result.isSuccess) {
                        navController.navigate("chatGraph") {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }else {
                    val error = result.exceptionOrNull()
                    errorMessage.value = when (error) {
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                            context.getString(R.string.email_already_in_use)
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                            context.getString(R.string.weak_password)
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            context.getString(R.string.invalid_email)
                        else -> context.getString(R.string.network_error)
                    }
                }
            }
        }
        Text(errorMessage.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}
