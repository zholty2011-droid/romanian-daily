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
fun CardsScreen(
    words: List<Word>,
    onSpeak: (String) -> Unit,
    onAnswer: (wordId: String, isKnown: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Нет карточек для повторения.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    if (currentIndex >= words.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Все карточки пройдены!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Вы повторили ${words.size} карточек.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    currentIndex = 0
                    isRevealed = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Повторить сначала", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("К проверке", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val word = words[currentIndex]
    val progress = (currentIndex + 1).toFloat() / words.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Карточка ${currentIndex + 1}/${words.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProgressBar(progress = progress)

        // Card Container
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { isRevealed = !isRevealed }
                .testTag("flashcard_body"),
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 38.sp),
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

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isRevealed) {
                        Text(
                            text = word.translation,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Нажмите, чтобы увидеть перевод",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Action choices
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
                        onAnswer(word.id, false)
                        currentIndex++
                        isRevealed = false
                    },
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
                        onAnswer(word.id, true)
                        currentIndex++
                        isRevealed = false
                    },
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
