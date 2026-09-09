package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.aistudio.romaniandaily.data.model.DialogLine
import com.aistudio.romaniandaily.data.model.DialogScenario
import com.aistudio.romaniandaily.data.repository.CitizenshipData
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun DialogsScreen(
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
    initialScenarioId: String? = null,
    onFinish: (() -> Unit)? = null,
    finishButtonText: String = "Далее: Предложения",
    modifier: Modifier = Modifier
) {
    var selectedScenarioId by remember { mutableStateOf<String?>(initialScenarioId) }
    val scenario = CitizenshipData.dialogs.find { it.id == selectedScenarioId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        TopBackButton(onBack = {
            if (scenario != null && initialScenarioId == null) selectedScenarioId = null else onBack()
        })

        Spacer(modifier = Modifier.height(6.dp))

        if (scenario != null) {
            // Detailed Active Dialog View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${scenario.icon} ${scenario.titleRu}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = scenario.titleRo,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scenario.lines) { line ->
                    DialogBubble(line = line, onSpeak = onSpeak)
                }
            }

            if (onFinish != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Text(finishButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // List of Scenarios
            Text(
                text = "Живые диалоги",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Практические сценарии для жизни в Румынии с озвучкой",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(CitizenshipData.dialogs) { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScenarioId = item.id },
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = item.icon, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.titleRu,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.titleRo,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
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
}

@Composable
private fun DialogBubble(
    line: DialogLine,
    onSpeak: (String) -> Unit
) {
    val isUser = line.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = line.speaker,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.ro,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (line.transcription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "[${line.transcription}]",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = line.ru,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onSpeak(line.ro) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUser) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Озвучить реплику",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
