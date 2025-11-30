package com.example.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.ui.theme.HintText
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.messenger.ui.theme.DirtyWhite
import com.example.messenger.ui.theme.DirtyWhite2
import com.example.messenger.ui.theme.Gray

@Composable
fun MyButton(text: String, modifier: Modifier= Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor =  colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ),
        modifier = modifier//.fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ){
        Text(text = text, color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun MyIconButton(icon: ImageVector, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Send",
        )
    }
}

@Composable
fun RemoveButton(onClick: () -> Unit){
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(25.dp) // размер кнопки
                .background(Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = Color.White,
                modifier = Modifier.fillMaxSize()
                    .background(Color(0x40FFFFFF))
            )
        }
    }
}
@Composable
fun buttonBack(onClick: () -> Unit,enabled: Boolean = true) {
    Button(//modifier = Modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        onClick = onClick,
        enabled =enabled,
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Назад"
        )
    }
}

@Composable
fun MyTextField(
    text: String,
    hint: String,
    onValueChange: (String) -> Unit
){
    TextField(
        modifier = Modifier.fillMaxWidth()
            .border(2.dp, Gray, RoundedCornerShape(10.dp)),
        value = text,
        onValueChange = onValueChange,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DirtyWhite2,
            unfocusedContainerColor = DirtyWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        placeholder = { HintText(hint) },
    )
}

@Composable
fun MyTextField2(
    text: String,
    hint: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = modifier,
        value = text,
        onValueChange = onValueChange,
        placeholder = { Text(hint) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.primary,
            unfocusedTextColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedPlaceholderColor = Color.LightGray,
            unfocusedPlaceholderColor = Color.LightGray
        )
    )
}
@Composable
fun SearchBar(query: String,
              onQueryChange: (String) -> Unit){
    TextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистить"
                    )
                }
            }
        },
        placeholder = { HintText("Поиск...") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
    )
}