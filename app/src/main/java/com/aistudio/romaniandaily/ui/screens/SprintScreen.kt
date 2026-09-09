package com.aistudio.romaniandaily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
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
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

data class SprintTask(
    val word: Word,
    val displayedTranslation: String,
    val isTrueMatch: Boolean
)

@Composable
fun SprintScreen(
    wordsPool: List<Word>,
    onSpeak: (String) -> Unit,
    onFinish: (score: Int) -> Unit,
    onBack: () -> Unit,
    finishButtonText: String = "Вернуться в меню",
    durationSeconds: Int = 60,
    modifier: Modifier = Modifier
) {
    val totalTimeSeconds = durationSeconds
    var timeLeft by remember { mutableIntStateOf(totalTimeSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }

    val tasks = remember(wordsPool) {
        val list = mutableListOf<SprintTask>()
        val pool = if (wordsPool.size < 6) wordsPool else wordsPool.shuffled()
        for (i in 0 until 100) {
            val target = pool[i % pool.size]
            val isTrue = Random.nextBoolean()
            val displayedTr = if (isTrue) {
                target.translation
            } else {
                val other = pool.filter { it.id != target.id }.randomOrNull()?.translation ?: target.translation
                other
            }
            list.add(SprintTask(word = target, displayedTranslation = displayedTr, isTrueMatch = displayedTr == target.translation))
        }
        list
    }

    var currentTaskIndex by remember { mutableIntStateOf(0) }

    // Countdown Timer
    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft <= 0) {
            isRunning = false
        }
    }

    if (!isRunning || timeLeft <= 0) {
        // Game Over Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⏱️ Время вышло!",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                        text = "ИТОГОВЫЙ СЧЁТ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$score",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "баллов за 60 секунд",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    timeLeft = totalTimeSeconds
                    score = 0
                    streak = 0
                    currentTaskIndex = 0
                    isRunning = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape
            ) {
                Text("Сыграть ещё раз", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onFinish(score)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape
            ) {
                Text(finishButtonText, fontSize = 16.sp)
            }
        }
        return
    }

    val currentTask = tasks[currentTaskIndex % tasks.size]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TopBackButton(onBack = onBack)

            Spacer(modifier = Modifier.height(8.dp))

            // Timer and Score bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (timeLeft < 10) BadRedSoftLight else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Таймер",
                            tint = if (timeLeft < 10) BadRedLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${timeLeft}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 10) BadRedLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Очки: $score  🔥 $streak",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Word Flash Card
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ВЕРЕН ЛИ ЭТОТ ПЕРЕВОД?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentTask.word.word,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (currentTask.word.transcription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[${currentTask.word.transcription}]",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(modifier = Modifier.width(120.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = currentTask.displayedTranslation,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // True / False Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val isCorrect = !currentTask.isTrueMatch
                    if (isCorrect) {
                        score += 10 + (streak * 2)
                        streak++
                    } else {
                        streak = 0
                    }
                    currentTaskIndex++
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BadRedSoftLight,
                    contentColor = BadRedLight
                )
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Неверно", modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("НЕВЕРНО", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val isCorrect = currentTask.isTrueMatch
                    if (isCorrect) {
                        score += 10 + (streak * 2)
                        streak++
                        onSpeak(currentTask.word.word)
                    } else {
                        streak = 0
                    }
                    currentTaskIndex++
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoodGreenSoftLight,
                    contentColor = GoodGreenLight
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Верно", modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ВЕРНО", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
