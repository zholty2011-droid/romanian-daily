package com.aistudio.romaniandaily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import com.aistudio.romaniandaily.data.model.CitizenshipQA
import com.aistudio.romaniandaily.data.model.CitizenshipTopic
import com.aistudio.romaniandaily.data.repository.CitizenshipData
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun CitizenshipScreen(
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Присяга, 1 = Вопросы комиссии
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var slowMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        TopBackButton(onBack = onBack)

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Присяга и Гражданство",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Jurământ & Interviu ANC",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (slowMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { slowMode = !slowMode }
            ) {
                Text(
                    text = if (slowMode) "🐢 0.75x" else "🐇 1.0x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (slowMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Текст присяги / Вопросы комиссии
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selectedTab == 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
                    .testTag("tab_oath")
            ) {
                Text(
                    text = "📜 Текст присяги",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selectedTab == 1) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
                    .testTag("tab_interview")
            ) {
                Text(
                    text = "⚖️ Вопросы комиссии",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Oath text view
            OathSection(
                onSpeak = onSpeak,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Interview Questions view
            InterviewSection(
                topics = CitizenshipData.topics,
                selectedTopicId = selectedTopicId,
                onSelectTopic = { selectedTopicId = it },
                onSpeak = onSpeak,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OathSection(
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Flag Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ОФИЦИАЛЬНЫЙ ТЕКСТ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        IconButton(
                            onClick = { onSpeak(CitizenshipData.oathTextRo) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Прослушать всю присягу",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jurământul de credință față de România",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Произносится на церемонии перед комиссией ANC наизусть или по тексту с идеальным произношением.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stanza Cards
        items(CitizenshipData.oathLines.indices.toList()) { idx ->
            val pair = CitizenshipData.oathLines[idx]
            val phon = CitizenshipData.oathPhonetics[idx]

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Строка ${idx + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        IconButton(
                            onClick = { onSpeak(pair.first) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Озвучить строку",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pair.first,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "[$phon]",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pair.second,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Useful advice card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 СОВЕТ ДЛЯ СДАЧИ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Произносите «Jur» [Жур] мягко, без грубого «Дж».\n" +
                               "• Букву «ă» в «să apăr» произносите как приглушённый средний звук между «а» и «э».\n" +
                               "• Держите уверенный зрительный контакт с комиссией и держите руку на сердце или положите на Конституцию по указанию чиновника.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InterviewSection(
    topics: List<CitizenshipTopic>,
    selectedTopicId: String?,
    onSelectTopic: (String?) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTopic = topics.find { it.id == selectedTopicId }

    if (activeTopic != null) {
        // Active Topic questions
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectTopic(null) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Ко всем темам",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${activeTopic.icon} ${activeTopic.titleRu}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeTopic.questions) { qa ->
                    QuestionAnswerCard(qa = qa, onSpeak = onSpeak)
                }
            }
        }
    } else {
        // Topics List
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Выберите блок вопросов:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(topics) { topic ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(topic.id) },
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = topic.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.titleRu,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = topic.titleRo,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${topic.questions.size} типовых вопросов с ответами",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionAnswerCard(
    qa: CitizenshipQA,
    onSpeak: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Question Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ВОПРОС КОМИССИИ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = qa.qRo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (qa.qTrans.isNotEmpty()) {
                        Text(
                            text = "[${qa.qTrans}]",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = qa.qRu,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onSpeak(qa.qRo) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Озвучить вопрос",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // Answer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ВАШ ОТВЕТ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoodGreenLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = qa.aRo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (qa.aTrans.isNotEmpty()) {
                        Text(
                            text = "[${qa.aTrans}]",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = qa.aRu,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onSpeak(qa.aRo) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoodGreenSoftLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Озвучить ответ",
                        tint = GoodGreenLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (qa.tip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ${qa.tip}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
