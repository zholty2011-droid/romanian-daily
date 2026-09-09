package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.Word
import com.aistudio.romaniandaily.ui.components.SpeakerButton
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun DictionaryScreen(
    words: List<Word>,
    learnedIds: Set<String>,
    wrongIds: Set<String>,
    onSpeak: (String) -> Unit,
    onToggleLearned: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("all") } // "all", "new", "wrong"

    val filtered = remember(words, searchQuery, selectedTab, learnedIds, wrongIds) {
        val q = searchQuery.trim().lowercase()
        words.filter { w ->
            val matchQuery = q.isEmpty() ||
                    w.word.lowercase().contains(q) ||
                    w.translation.lowercase().contains(q)
            val matchTab = when (selectedTab) {
                "new" -> !learnedIds.contains(w.id)
                "wrong" -> wrongIds.contains(w.id)
                else -> true
            }
            matchQuery && matchTab
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBackButton(onBack = onBack)

        Text(
            text = "Слова (${words.size})",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск слова или перевода...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dictionary_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "all" to "Все (${words.size})",
                "new" to "Новые (${words.size - learnedIds.size})",
                "wrong" to "Ошибки (${wrongIds.size})"
            )
            tabs.forEach { (key, label) ->
                val isSel = selectedTab == key
                Surface(
                    shape = CircleShape,
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedTab = key }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // List of Words
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { word ->
                val isLearned = learnedIds.contains(word.id)
                val isWrong = wrongIds.contains(word.id)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleLearned(word.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isLearned) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Выучено",
                                    tint = GoodGreenLight
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Circle,
                                    contentDescription = "Не выучено",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isWrong) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "⚠️", fontSize = 12.sp)
                                }
                            }

                            if (word.transcription.isNotEmpty()) {
                                Text(
                                    text = "[${word.transcription}]",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = word.translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        SpeakerButton(onClick = { onSpeak(word.word) }, isGhost = true)
                    }
                }
            }
        }
    }
}
