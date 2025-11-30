package com.example.messenger.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.messenger.User

@Composable
fun SignUp(navController: NavController, viewModel: MainViewModel) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    //val location = remember { mutableStateOf("") }
    val db = DataSign()
    val db2 = FireBase()
    Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        MyTextField(email.value,stringResource(R.string.hint_email)){ email.value = it }
        MyTextField(password.value,stringResource(R.string.hint_password)){ password.value = it }
        MyTextField(name.value,stringResource(R.string.hint_name)){ name.value = it }
       // MyTextField(location.value,stringResource(R.string.hint_location)){ location.value = it }
        MyButton(stringResource(R.string.btn_signup2)) {
            db.signUp(email.value,password.value,
                        uid = { uid ->
                            if (uid != null) {
                                db2.signUpInfo(uid, name.value, "location.value")
                                viewModel.loggedInUser = User(uid, name.value, "location.value", "", emptyList())
                                viewModel.addUser(uid, name.value, "location.value", emptyList())
                                navController.navigate(Screen.Messenger.route)
                            }
                        }
            )
        }
    }
}
