package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.Word
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.SpeakerButton
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun LessonScreen(
    words: List<Word>,
    stepTitle: String,
    onSpeak: (String) -> Unit,
    onMarkWord: (wordId: String, isKnown: Boolean) -> Unit,
    onFinishStep: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Урок пуст.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    val word = words[currentIndex.coerceIn(0, words.size - 1)]
    val progress = (currentIndex + 1).toFloat() / words.size.toFloat()
    val isLast = currentIndex == words.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Back button, Step counter, Progress
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TopBackButton(onBack = onBack)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$stepTitle • ${currentIndex + 1}/${words.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ProgressBar(progress = progress)
        }

        // Central Word Card: Fills available space, perfectly centered, guaranteed to fit on screen
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 34.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SpeakerButton(onClick = { onSpeak(word.word) })
                    }

                    if (word.transcription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "[${word.transcription}]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    if (isRevealed) {
                        Text(
                            text = word.translation,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Нажмите кнопку внизу для перевода",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom Action Controls: Fixed height dock, never causes overflow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            if (!isRevealed) {
                Button(
                    onClick = { isRevealed = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reveal_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Показать перевод",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BadRedSoftLight,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable {
                                onMarkWord(word.id, false)
                                if (isLast) {
                                    onFinishStep()
                                } else {
                                    currentIndex++
                                    isRevealed = false
                                }
                            }
                            .testTag("dont_know_button"),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Не знаю",
                                fontWeight = FontWeight.Bold,
                                color = BadRedLight,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = GoodGreenSoftLight,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable {
                                onMarkWord(word.id, true)
                                if (isLast) {
                                    onFinishStep()
                                } else {
                                    currentIndex++
                                    isRevealed = false
                                }
                            }
                            .testTag("know_button"),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Знаю",
                                fontWeight = FontWeight.Bold,
                                color = GoodGreenLight,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
