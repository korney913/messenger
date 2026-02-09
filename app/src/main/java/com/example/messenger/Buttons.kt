package com.example.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.messenger.Screens.Settings

@Composable
fun MyButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ),
        modifier = modifier.height(55.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ButtonForSettings(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,      // Цвет панелей (светло-серый/графит)
            contentColor = colorScheme.onSurface       // Основной текст
        ),
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ButtonTextSize(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colorScheme.surface), // Фон в тон настроек
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onClick()
                Settings.textSizeScale.value = Settings.textSizeScale.value - 0.1f
            },
            enabled = Settings.textSizeScale.value > 0.7,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "-",
                tint = colorScheme.primary, // Кнопки управления - синие
            )
        }
        Text(
            stringResource(R.string.text_size_scale),
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
        IconButton(
            onClick = {
                onClick()
                Settings.textSizeScale.value = Settings.textSizeScale.value + 0.1f
            },
            enabled = Settings.textSizeScale.value < 1.5,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "+",
                tint = colorScheme.primary, // Кнопки управления - синие
            )
        }
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
            contentDescription = "Icon",
            tint = colorScheme.primary
        )
    }
}

@Composable
fun RemoveButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape) // Затемнение под крестиком
            )
        }
    }
}

@Composable
fun ButtonBack(onClick: () -> Unit, enabled: Boolean = true) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Назад",
            tint = colorScheme.onSurface
        )
    }
}

@Composable
fun MyTextField(
    text: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp)), // Тонкая рамка в стиле VK
        value = text,
        onValueChange = onValueChange,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceVariant,
            unfocusedContainerColor = colorScheme.background,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            cursorColor = colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(hint, color = colorScheme.secondary) },
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
        placeholder = { Text(hint, color = colorScheme.secondary) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            cursorColor = colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface
        )
    )
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск",
                tint = colorScheme.secondary // Серые иконки поиска
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистить",
                        tint = colorScheme.secondary
                    )
                }
            }
        },
        placeholder = { Text(stringResource(R.string.search), color = colorScheme.secondary) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface, // Округлая плашка поиска
            unfocusedContainerColor = colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            cursorColor = colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    )
}