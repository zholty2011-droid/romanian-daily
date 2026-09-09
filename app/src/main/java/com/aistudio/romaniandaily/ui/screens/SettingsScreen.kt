package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    currentSpeechRate: Float,
    isDarkTheme: Boolean,
    onSpeechRateChange: (Float) -> Unit,
    onTestSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rate by remember { mutableFloatStateOf(currentSpeechRate) }
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Text(
            text = "Настройки",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // TTS Speech Rate Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ОЗВУЧКА",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Скорость речи",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.2f", rate)}x",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = rate,
                    onValueChange = {
                        rate = it
                        onSpeechRateChange(it)
                    },
                    valueRange = 0.5f..1.3f,
                    steps = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speech_rate_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onTestSound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("test_sound_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Проверить звук", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Appearance Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Тема оформления",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkTheme) "Тёмная тема" else "Светлая тёплая бумага",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.testTag("theme_switch")
                )
            }
        }

        // Reset progress Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Сброс прогресса",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Сбросить историю, выученные слова и ударный режим.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("reset_progress_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Сбросить весь прогресс", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить весь прогресс?") },
            text = { Text("Все данные об изученных словах, ошибках и днях будут обнулены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    }
                ) {
                    Text("Сбросить", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
