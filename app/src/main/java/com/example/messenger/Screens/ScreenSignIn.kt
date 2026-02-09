package com.example.messenger.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.messenger.setUserOnline
import com.example.messenger.ui.theme.BlackPurple
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.launch

@Composable
fun SignIn(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val email = remember {mutableStateOf("wwww@gmail.com")}
    val password = remember {mutableStateOf("12345678")}
    val errorMessage = remember { mutableStateOf("") }
    val db = FireBase()
    Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.btn_sign_in),
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onBackground,
        )
        MyTextField(email.value,stringResource(R.string.hint_email)){ email.value = it }
        MyTextField(password.value,stringResource(R.string.hint_password)){ password.value = it }
        MyButton(stringResource(R.string.btn_sign_in), modifier = Modifier.fillMaxWidth()) {
            scope.launch {
                val result = db.signIn(email.value, password.value)
                if (result.isSuccess) {                    setUserOnline(true)
                    navController.navigate("chatGraph") {
                        popUpTo(Screen.LogIn.route) { inclusive = true }
                    }
                }else { val error = result.exceptionOrNull()
                    errorMessage.value = when (error) {
                        is FirebaseAuthInvalidCredentialsException ->
                            context.getString(R.string.wrong_password_or_email)
                        else -> context.getString(R.string.network_error)
                    }
                }
            }
        }
        Text(errorMessage.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Text(
                text = stringResource(R.string.btn_signup),
        color = BlackPurple,
        modifier = Modifier
            .clickable {
                navController.navigate(Screen.SignUp.route)
            }
        )
    }
}