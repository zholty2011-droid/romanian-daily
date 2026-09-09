package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.QuizItem
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.SpeakerButton
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun QuizScreen(
    items: List<QuizItem>,
    isRoToRu: Boolean,
    isExamMode: Boolean,
    onSpeak: (String) -> Unit,
    onAnswer: (itemIndex: Int, selectedWordId: String, isCorrect: Boolean) -> Unit,
    onRetryWrong: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Нет вопросов для теста.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    // Finished
    if (currentIndex >= items.size) {
        val correctCount = items.count { it.isCorrect == true }
        val pct = if (items.isNotEmpty()) (100 * correctCount) / items.size else 0
        val grade = when {
            pct >= 90 -> "A"
            pct >= 75 -> "B"
            pct >= 60 -> "C"
            pct >= 40 -> "D"
            else -> "F"
        }
        val hint = when {
            pct >= 90 -> "Отлично! Материал усвоен превосходно."
            pct >= 70 -> "Хорошо, рекомендуем разобрать ошибки."
            else -> "Стоит вернуться к карточкам и повторить слова."
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isExamMode) "Экзамен" else "Результат",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = grade,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = if (pct >= 75) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$correctCount/${items.size} • $pct%",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))
            if (correctCount < items.size) {
                Button(
                    onClick = onRetryWrong,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("retry_wrong_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Только ошибки", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("quiz_finish_back_button"),
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

    val q = items[currentIndex]
    val word = q.word
    val prompt = if (isRoToRu) word.word else word.translation
    val progress = (currentIndex + 1).toFloat() / items.size.toFloat()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Вопрос ${currentIndex + 1}/${items.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isRoToRu) "RO → RU" else "RU → RO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ProgressBar(progress = progress)

        // Question card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (isRoToRu && !isExamMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SpeakerButton(onClick = { onSpeak(word.word) })
                    }
                }
                if (isRoToRu && !isExamMode && word.transcription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "[${word.transcription}]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Choices
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEach { opt ->
                val isPicked = selectedId == opt.id
                val isRight = opt.id == word.id
                val label = if (isRoToRu) opt.translation else opt.word

                val (bgColor, textColor) = when {
                    selectedId == null -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                    isRight -> GoodGreenSoftLight to GoodGreenLight
                    isPicked -> BadRedSoftLight to BadRedLight
                    else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedId == null) {
                            selectedId = opt.id
                            val correct = opt.id == word.id
                            onAnswer(currentIndex, opt.id, correct)
                        },
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedId != null) {
            Button(
                onClick = {
                    currentIndex++
                    selectedId = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("quiz_next_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Далее", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
