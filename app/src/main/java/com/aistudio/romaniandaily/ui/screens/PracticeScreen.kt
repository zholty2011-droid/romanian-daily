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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun PracticeScreen(
    currentDay: Int,
    wrongCount: Int,
    unknownExamplesCount: Int,
    onStartTestDay: (isRoToRu: Boolean, count: Int, choicesCount: Int) -> Unit,
    onStartTestAllDays: (isRoToRu: Boolean, count: Int, choicesCount: Int) -> Unit,
    onStartCardsDay: () -> Unit,
    onStartExamplesDay: (onlyUnknown: Boolean) -> Unit,
    onStartWrite: (isRussian: Boolean) -> Unit,
    onStartListening: () -> Unit,
    onStartSentenceBuilder: () -> Unit,
    onStartSprint: () -> Unit,
    onStartCitizenship: () -> Unit,
    onStartDialogs: () -> Unit,
    onStartGrammarQuiz: () -> Unit,
    onStartGrammarOverview: () -> Unit,
    onStartPhrases: () -> Unit,
    onStartExam: () -> Unit,
    onRetryWrong: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRoToRu by remember { mutableStateOf(true) }
    var selectedCount by remember { mutableIntStateOf(12) }
    var choicesCount by remember { mutableIntStateOf(4) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Проверка",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Configuration Card: Direction & Options
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "НАПРАВЛЕНИЕ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectPill(
                        text = "Румынский → Русский",
                        selected = isRoToRu,
                        onClick = { isRoToRu = true },
                        modifier = Modifier.weight(1f)
                    )
                    SelectPill(
                        text = "Русский → Румынский",
                        selected = !isRoToRu,
                        onClick = { isRoToRu = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Вопросов: $selectedCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(8, 12, 20).forEach { cnt ->
                            SelectMiniPill(
                                text = "$cnt",
                                selected = selectedCount == cnt,
                                onClick = { selectedCount = cnt }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Вариантов: $choicesCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4, 6).forEach { opt ->
                            SelectMiniPill(
                                text = "$opt",
                                selected = choicesCount == opt,
                                onClick = { choicesCount = opt }
                            )
                        }
                    }
                }
            }
        }

        // Action Options List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // New High-Priority modes: Citizenship & Dialogs
            PracticeOptionTile(
                title = "🇷🇴 Присяга и Гражданство (ANC)",
                subtitle = "Текст присяги, транскрипция, вопросы комиссии",
                onClick = onStartCitizenship,
                isHighlight = true,
                testTag = "btn_citizenship"
            )
            PracticeOptionTile(
                title = "💬 Живые диалоги по ролям",
                subtitle = "В кафе, аэропорту, у врача, в магазине",
                onClick = onStartDialogs,
                testTag = "btn_dialogs"
            )
            PracticeOptionTile(
                title = "🎧 Аудирование (Слушай и выбирай)",
                subtitle = "Определение румынских слов на слух",
                onClick = onStartListening,
                testTag = "btn_listening"
            )
            PracticeOptionTile(
                title = "🧩 Конструктор предложений",
                subtitle = "Сборка румынских фраз из блоков-слов",
                onClick = onStartSentenceBuilder,
                testTag = "btn_sentence_builder"
            )
            PracticeOptionTile(
                title = "⚡ Блиц-спринт (60 сек)",
                subtitle = "Быстрая игра: Верно или Неверно",
                onClick = onStartSprint,
                testTag = "btn_sprint"
            )

            PracticeOptionTile(
                title = "Тест дня — $choicesCount варианта",
                subtitle = "День $currentDay, $selectedCount вопросов",
                onClick = { onStartTestDay(isRoToRu, selectedCount, choicesCount) },
                testTag = "btn_test_day"
            )
            PracticeOptionTile(
                title = "Тест по открытым дням",
                subtitle = "Случайные слова по всем пройденным дням",
                onClick = { onStartTestAllDays(isRoToRu, selectedCount, choicesCount) },
                testTag = "btn_test_all"
            )
            PracticeOptionTile(
                title = "Примеры дня",
                subtitle = "Предложения с контекстом и озвучкой",
                onClick = { onStartExamplesDay(false) },
                testTag = "btn_examples_day"
            )
            if (unknownExamplesCount > 0) {
                PracticeOptionTile(
                    title = "Примеры, которые не помню ($unknownExamplesCount)",
                    subtitle = "Повторить отмеченные предложения",
                    onClick = { onStartExamplesDay(true) },
                    testTag = "btn_examples_unknown"
                )
            }
            PracticeOptionTile(
                title = "Карточки дня",
                subtitle = "Флешкарты: слово → перевод",
                onClick = onStartCardsDay,
                testTag = "btn_cards_day"
            )
            PracticeOptionTile(
                title = "Написание — введи слово по-румынски",
                subtitle = "Тренировка с буквами и диакритикой",
                onClick = { onStartWrite(false) },
                testTag = "btn_write_ro"
            )
            PracticeOptionTile(
                title = "Написать перевод",
                subtitle = "Румынское слово → введи русский перевод",
                onClick = { onStartWrite(true) },
                testTag = "btn_write_ru"
            )
            PracticeOptionTile(
                title = "Тест по грамматике",
                subtitle = "Вопросы с объяснением правил",
                onClick = onStartGrammarQuiz,
                testTag = "btn_grammar_quiz"
            )
            PracticeOptionTile(
                title = "Грамматика по дням",
                subtitle = "Справочник правил",
                onClick = onStartGrammarOverview,
                testTag = "btn_grammar_rules"
            )
            PracticeOptionTile(
                title = "100 фраз на каждый день",
                subtitle = "Разговорные фразы для жизни",
                onClick = onStartPhrases,
                testTag = "btn_phrases"
            )
            if (wrongCount > 0) {
                PracticeOptionTile(
                    title = "Разобрать ошибки ($wrongCount)",
                    subtitle = "Только слова, где были неверные ответы",
                    onClick = onRetryWrong,
                    isHighlight = true,
                    testTag = "btn_retry_wrong"
                )
            }
            PracticeOptionTile(
                title = "Экзамен — 20 слов без подсказок",
                subtitle = "Строгая проверка знаний",
                onClick = onStartExam,
                testTag = "btn_exam"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SelectPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
        )
    }
}

@Composable
private fun SelectMiniPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PracticeOptionTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isHighlight: Boolean = false,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isHighlight) BadRedSoftLight else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isHighlight) BadRedLight else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = if (isHighlight) BadRedLight.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
