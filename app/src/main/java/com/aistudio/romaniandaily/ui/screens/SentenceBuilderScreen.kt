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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
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
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

data class SentencePuzzle(
    val sentenceRo: String,
    val translationRu: String,
    val transcription: String = "",
    val words: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderScreen(
    wordsPool: List<Word>,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: (() -> Unit)? = null,
    finishButtonText: String = "Вернуться в меню",
    maxPuzzles: Int = 10,
    modifier: Modifier = Modifier
) {
    // Generate puzzles from word examples
    val puzzles = remember(wordsPool, maxPuzzles) {
        val list = mutableListOf<SentencePuzzle>()
        wordsPool.forEach { word ->
            word.examples.forEach { ex ->
                val cleanRo = ex.ro.trim().replace(Regex("[.,!?]"), "")
                val tokens = cleanRo.split("\\s+".toRegex()).filter { it.isNotBlank() }
                if (tokens.size in 3..8) {
                    list.add(
                        SentencePuzzle(
                            sentenceRo = ex.ro.trim(),
                            translationRu = ex.ru.trim(),
                            transcription = ex.transcription,
                            words = tokens
                        )
                    )
                }
            }
        }
        if (list.isEmpty()) {
            listOf(
                SentencePuzzle("Bună ziua, cum sunteți?", "Добрый день, как вы поживаете?", "Бунэ зиуа, кум сунтець?", listOf("Bună", "ziua", "cum", "sunteți")),
                SentencePuzzle("O cafea vă rog frumos", "Кофе, пожалуйста", "О кафеа вэ рог фрумос", listOf("O", "cafea", "vă", "rog", "frumos")),
                SentencePuzzle("Unde este stația de tren?", "Где находится вокзал?", "Унде есте стация де трен?", listOf("Unde", "este", "stația", "de", "tren")),
                SentencePuzzle("Eu învăț limba română zilnic", "Я учу румынский язык ежедневно", "Еу ынвэц лимба ромынэ зилник", listOf("Eu", "învăț", "limba", "română", "zilnic"))
            )
        } else {
            list.shuffled().take(maxPuzzles)
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userSelectedTokens by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var availableTokens by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var isChecked by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }

    fun loadPuzzle(index: Int) {
        if (index < puzzles.size) {
            val p = puzzles[index]
            val indexed = p.words.mapIndexed { i, w -> Pair(i, w) }.shuffled()
            availableTokens = indexed
            userSelectedTokens = emptyList()
            isChecked = false
            isCorrect = false
        }
    }

    LaunchedEffect(currentIndex, puzzles) {
        loadPuzzle(currentIndex)
    }

    val scrollState = rememberScrollState()

    if (currentIndex >= puzzles.size) {
        // Summary
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Отличная работа!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Собрано предложений: $correctCount из ${puzzles.size}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    currentIndex = 0
                    correctCount = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape
            ) {
                Text("Пройти ещё раз", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    if (onFinish != null) onFinish() else onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape
            ) {
                Text(finishButtonText)
            }
        }
        return
    }

    val puzzle = puzzles[currentIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TopBackButton(onBack = onBack)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Конструктор предложений",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${currentIndex + 1}/${puzzles.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Prompt Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "СОБЕРИТЕ ПЕРЕВОД",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = puzzle.translationRu,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isChecked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = puzzle.sentenceRo,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) GoodGreenLight else BadRedLight
                                )
                                if (puzzle.transcription.isNotEmpty()) {
                                    Text(
                                        text = "[${puzzle.transcription}]",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onSpeak(puzzle.sentenceRo) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Озвучить предложение",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Target Selected Row Area Header with Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ваш ответ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (userSelectedTokens.isNotEmpty() && !isChecked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Undo / Delete last word button
                        TextButton(
                            onClick = {
                                val lastToken = userSelectedTokens.last()
                                userSelectedTokens = userSelectedTokens.dropLast(1)
                                availableTokens = availableTokens + lastToken
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Удалить последнее слово",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Удалить",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Clear all button
                        IconButton(
                            onClick = {
                                availableTokens = availableTokens + userSelectedTokens
                                userSelectedTokens = emptyList()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Очистить все",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Target Selected Row Area
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 76.dp)
            ) {
                if (userSelectedTokens.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нажимайте на слова внизу, чтобы составить фразу",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        userSelectedTokens.forEach { tokenPair ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clickable(enabled = !isChecked) {
                                        // Return to available
                                        userSelectedTokens = userSelectedTokens.filter { it.first != tokenPair.first }
                                        availableTokens = availableTokens + tokenPair
                                    }
                            ) {
                                Text(
                                    text = tokenPair.second,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Available Word Tokens to Click
            Text(
                text = "Доступные слова:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTokens.forEach { tokenPair ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .clickable(enabled = !isChecked) {
                                availableTokens = availableTokens.filter { it.first != tokenPair.first }
                                userSelectedTokens = userSelectedTokens + tokenPair
                            }
                    ) {
                        Text(
                            text = tokenPair.second,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Action Button
        Column(modifier = Modifier.padding(top = 20.dp)) {
            if (!isChecked) {
                Button(
                    onClick = {
                        val builtSentence = userSelectedTokens.joinToString(" ") { it.second }.lowercase().trim()
                        val targetTokens = puzzle.words.joinToString(" ") { it }.lowercase().trim()
                        val match = builtSentence == targetTokens

                        isCorrect = match
                        isChecked = true
                        if (match) {
                            correctCount++
                            onSpeak(puzzle.sentenceRo)
                        }
                    },
                    enabled = userSelectedTokens.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Проверить", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        currentIndex++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrect) GoodGreenLight else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (currentIndex == puzzles.size - 1) "Посмотреть результат" else "Следующее предложение →",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
