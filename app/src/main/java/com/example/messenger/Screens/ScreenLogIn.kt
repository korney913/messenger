package com.example.messenger.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.messenger.DataBase.FireBase
import com.example.messenger.DataBase.DataSign
import com.example.messenger.MainViewModel
import com.example.messenger.MyButton
import com.example.messenger.MyTextField
import com.example.messenger.R
import com.example.messenger.Screen
import com.example.messenger.ui.theme.BlackPurple

@Composable
fun SignIn(navController: NavController, viewModel: MainViewModel) {
    val email = remember {mutableStateOf("wwww@gmail.com")}
    val password = remember {mutableStateOf("12345678")}
    val db = DataSign()
    val db2= FireBase()
    Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        MyTextField(email.value,stringResource(R.string.hint_email)){ email.value = it }
        MyTextField(password.value,stringResource(R.string.hint_password)){ password.value = it }
        MyButton(stringResource(R.string.btn_signin)) {
            db.signIn(email.value, password.value,
                    uid = { uid ->
                if (uid != null) {
                    db2.getInfo(uid){ user ->
                        viewModel.loggedInUser= user!!
                        navController.navigate(Screen.Messenger.route)
                    }
                }
            })
       }
        Text(
                text = stringResource(R.string.btn_signup),
        color = BlackPurple,
        modifier = Modifier
            // .padding(16.dp)
            .clickable {
                navController.navigate(Screen.SignUp.route)
            }
        )
    }
}