package com.aistudio.romaniandaily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.Word
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun HomeScreen(
    currentDay: Int,
    dailyWords: List<Word>,
    allWordsCount: Int,
    learnedCount: Int,
    dueSrsCount: Int,
    allDays: List<Int>,
    onSelectDay: (Int) -> Unit,
    onStartDay: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDays by remember { mutableStateOf(false) }
    val currentDayLearned = dailyWords.count { w -> learnedCount > 0 } // handled by parent
    val dayLearnedCount = remember(dailyWords, learnedCount) {
        // approximate or pass directly
        dailyWords.size.coerceAtMost(10)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_hero_card"),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "ROMANIAN DAILY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "День $currentDay",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.background
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "7 этапов: Слова • Письмо • Грамматика • Аудирование • Конструктор • Диалог • Предложения",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "В курсе: $allWordsCount слов",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$learnedCount выучено",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                val pct = if (allWordsCount > 0) learnedCount.toFloat() / allWordsCount.toFloat() else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pct.coerceIn(0.02f, 1f))
                            .clip(CircleShape)
                            .background(Color(0xFFE8C9A2))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                if (dueSrsCount > 0) {
                    Text(
                        text = "Сегодня к повторению: $dueSrsCount слов",
                        fontSize = 13.sp,
                        color = Color(0xFFE8C9A2),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Button(
                    onClick = onStartDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_day_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Начать день",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Days Grid Toggle
        AnimatedVisibility(visible = showDays) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(vertical = 4.dp),
                shadowElevation = 1.dp
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allDays) { day ->
                        val isSelected = day == currentDay
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    onSelectDay(day)
                                    showDays = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.background
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons list
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionTile(
                title = "🇷🇴 Присяга и Гражданство (ANC)",
                subtitle = "Текст присяги, произношение, вопросы комиссии",
                onClick = { onNavigate("citizenship") },
                testTag = "action_citizenship"
            )
            ActionTile(
                title = "💬 Живые диалоги",
                subtitle = "В кафе, аэропорту, у врача, в магазине",
                onClick = { onNavigate("dialogs") },
                testTag = "action_dialogs"
            )
            ActionTile(
                title = if (showDays) "Скрыть список дней" else "Выбрать день ($currentDay)",
                onClick = { showDays = !showDays },
                testTag = "action_toggle_days"
            )
            ActionTile(
                title = "Проверка",
                subtitle = "Тесты, карточки, письмо, грамматика",
                onClick = { onNavigate("practice") },
                testTag = "action_practice"
            )
            ActionTile(
                title = "Все фразы",
                subtitle = "100 фраз для дома и улицы",
                onClick = { onNavigate("phrases") },
                testTag = "action_phrases"
            )
            ActionTile(
                title = "Словарь",
                subtitle = "Все слова с озвучкой и поиском",
                onClick = { onNavigate("dictionary") },
                testTag = "action_dictionary"
            )
            ActionTile(
                title = "Свои слова",
                subtitle = "Добавь свои списки слов",
                onClick = { onNavigate("import") },
                testTag = "action_custom_words"
            )
            ActionTile(
                title = "Статистика",
                subtitle = "Интервалы повторений и календарь 8 недель",
                onClick = { onNavigate("stats") },
                testTag = "action_stats"
            )
            ActionTile(
                title = "Настройки",
                subtitle = "Скорость озвучки, озвучка и тема",
                onClick = { onNavigate("settings") },
                testTag = "action_settings"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
