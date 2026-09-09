package com.aistudio.romaniandaily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.Word
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

data class ListeningQuizItem(
    val word: Word,
    val options: List<Word>
)

@Composable
fun ListeningScreen(
    wordsPool: List<Word>,
    onSpeak: (String) -> Unit,
    onAnswer: (wordId: String, isCorrect: Boolean) -> Unit,
    onBack: () -> Unit,
    onFinish: (() -> Unit)? = null,
    finishButtonText: String = "Вернуться в меню",
    maxQuestions: Int = 12,
    modifier: Modifier = Modifier
) {
    val items = remember(wordsPool, maxQuestions) {
        val shuffled = wordsPool.shuffled()
        val questions = shuffled.take(maxQuestions)
        questions.map { target ->
            val wrong = wordsPool.filter { it.id != target.id }.shuffled().take(3)
            val allOptions = (wrong + target).shuffled()
            ListeningQuizItem(word = target, options = allOptions)
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var correctScore by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    // Reset per-question state and auto speak when question changes
    LaunchedEffect(currentIndex) {
        selectedOptionIndex = null
        isAnswered = false
        if (currentIndex < items.size) {
            onSpeak(items[currentIndex].word.word)
        }
    }

    if (items.isEmpty() || currentIndex >= items.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Аудирование завершено!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Правильно на слух: $correctScore из ${items.size}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    if (onFinish != null) onFinish() else onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape
            ) {
                Text(finishButtonText, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val item = items[currentIndex]
    val correctIndex = item.options.indexOfFirst { it.id == item.word.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TopBackButton(onBack = onBack)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Аудирование: Слушай и выбирай",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${currentIndex + 1}/${items.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ProgressBar(progress = (currentIndex + 1).toFloat() / items.size.toFloat())

            // Listening Audio Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "СЛУШАЙТЕ И ВЫБЕРИТЕ ПЕРЕВОД",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Large Audio Play Button
                    IconButton(
                        onClick = { onSpeak(item.word.word) },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Воспроизвести румынское слово",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Нажмите, чтобы повторить звук",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isAnswered) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = item.word.word,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.word.transcription.isNotEmpty()) {
                            Text(
                                text = "[${item.word.transcription}]",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOptionIndex == idx
                    val isTarget = idx == correctIndex

                    val (bgColor, textColor) = when {
                        !isAnswered -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                        isTarget -> GoodGreenSoftLight to GoodGreenLight
                        isSelected -> BadRedSoftLight to BadRedLight
                        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswered) {
                                selectedOptionIndex = idx
                                isAnswered = true
                                val correct = idx == correctIndex
                                if (correct) correctScore++
                                onAnswer(item.word.id, correct)
                                onSpeak(item.word.word)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = opt.translation,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAnswered && isTarget) {
                                Text(
                                    text = "✓",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoodGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        if (isAnswered) {
            Button(
                onClick = {
                    currentIndex++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 16.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (currentIndex == items.size - 1) "Посмотреть результат" else "Дальше →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(68.dp))
        }
    }
}
