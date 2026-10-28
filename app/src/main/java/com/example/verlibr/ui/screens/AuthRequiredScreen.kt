package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.ui.theme.Taupe

/** Заглушка для гостя: вход выполняется кнопкой «Вход» в шапке. */
@Composable
fun AuthRequiredScreen(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            color = Taupe,
            textAlign = TextAlign.Center
        )
    }
}
