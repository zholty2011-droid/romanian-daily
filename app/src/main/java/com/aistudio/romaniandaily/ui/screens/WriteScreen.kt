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
import com.aistudio.romaniandaily.data.model.Word
import com.aistudio.romaniandaily.ui.components.LetterPad
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.SpeakerButton
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun WriteScreen(
    words: List<Word>,
    isWriteRussian: Boolean, // false = write Romanian, true = write Russian translation
    stepTitle: String? = null,
    finishButtonText: String = "Далее: Грамматика",
    onSpeak: (String) -> Unit,
    onAnswer: (wordId: String, isCorrect: Boolean) -> Unit,
    onFinish: (() -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Нет слов для упражнения.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userText by remember { mutableStateOf("") }
    var hintLevel by remember { mutableIntStateOf(0) }
    var isChecked by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    if (currentIndex >= words.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Упражнение завершено!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Вы успешно попрактиковали написание слов.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            if (onFinish != null) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("finish_day_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(finishButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
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
        }
        return
    }

    val word = words[currentIndex]
    val target = if (isWriteRussian) word.translation.trim().lowercase() else word.word.trim().lowercase()
    val prompt = if (isWriteRussian) word.word else word.translation
    val progress = (currentIndex + 1).toFloat() / words.size.toFloat()

    // Bank letters: target characters shuffled + some extra Romanian letters
    val bankLetters = remember(word, isWriteRussian) {
        val chars = target.filter { it.isLetter() }.map { it.toString() }.toMutableList()
        val extra = if (isWriteRussian) listOf("а", "о", "е", "и", "т", "н", "с", "р")
        else listOf("a", "e", "i", "o", "u", "r", "s", "t", "ă", "î")
        chars.addAll(extra.shuffled().take(4))
        chars.shuffled()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBackButton(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (stepTitle != null) "$stepTitle • ${currentIndex + 1}/${words.size}"
                else "Слово ${currentIndex + 1}/${words.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isWriteRussian) "Перевод" else "По-румынски",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ProgressBar(progress = progress)

        // Prompt Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 30.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (isWriteRussian) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SpeakerButton(onClick = { onSpeak(word.word) })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Mask indicator (e.g. 5 букв or 2 слова)
                val wordsCount = target.split("\\s+".toRegex()).size
                val letterCount = target.filter { it.isLetter() }.length
                val countText = if (wordsCount > 1) "$wordsCount слова • $letterCount букв" else "$letterCount букв"
                Text(
                    text = countText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Progressive Hint display
                if (hintLevel > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val hintText = when (hintLevel) {
                        1 -> "Начинается на: ${target.take(1)}..."
                        2 -> "Первые буквы: ${target.take(2)}..."
                        else -> "Полное слово: $target"
                    }
                    Text(
                        text = hintText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Input and Letter pad with dedicated space support
        LetterPad(
            currentText = userText,
            bankLetters = bankLetters,
            onLetterClick = { ch ->
                if (!isChecked) {
                    userText += ch
                }
            },
            onSpaceClick = {
                if (!isChecked && userText.isNotEmpty() && !userText.endsWith(" ")) {
                    userText += " "
                }
            },
            onBackspace = {
                if (!isChecked && userText.isNotEmpty()) {
                    userText = userText.dropLast(1)
                }
            }
        )

        // Software text input alternative
        OutlinedTextField(
            value = userText,
            onValueChange = { if (!isChecked) userText = it },
            placeholder = { Text("Или введите с клавиатуры...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("write_input_field"),
            singleLine = true,
            enabled = !isChecked
        )

        // Hint button
        if (!isChecked && hintLevel < 3) {
            TextButton(
                onClick = { hintLevel++ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Ещё подсказка",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Feedback if checked
        if (isChecked) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSuccess) GoodGreenSoftLight else BadRedSoftLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSuccess) "Правильно!" else "Неверно. Правильный ответ: $target",
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) GoodGreenLight else BadRedLight,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action button
        if (!isChecked) {
            Button(
                onClick = {
                    val cleanUser = userText.trim().replace("\\s+".toRegex(), " ").lowercase()
                    val cleanTarget = target.trim().replace("\\s+".toRegex(), " ").lowercase()
                    isSuccess = cleanUser == cleanTarget
                    isChecked = true
                    onAnswer(word.id, isSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("write_check_button"),
                shape = CircleShape,
                enabled = userText.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Проверить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = {
                    currentIndex++
                    userText = ""
                    isChecked = false
                    hintLevel = 0
                    isSuccess = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("write_next_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Далее", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
