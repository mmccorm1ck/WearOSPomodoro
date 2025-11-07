package com.example.pomodoro.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun PomodoroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}