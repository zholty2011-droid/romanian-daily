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
import com.aistudio.romaniandaily.data.model.ExampleItem
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.SpeakerButton
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun ExamplesScreen(
    items: List<ExampleItem>,
    title: String,
    onSpeak: (String) -> Unit,
    onMark: (wordId: String, ro: String, isKnown: Boolean) -> Unit,
    onBack: () -> Unit,
    onFinish: (() -> Unit)? = null,
    finishButtonText: String = "Завершить день",
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Нет примеров для просмотра.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    if (currentIndex >= items.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Все примеры пройдены!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Вы повторили ${items.size} предложений в контексте.",
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

    val item = items[currentIndex]
    val ex = item.example
    val progress = (currentIndex + 1).toFloat() / items.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TopBackButton(onBack = onBack)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$title • ${currentIndex + 1}/${items.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ProgressBar(progress = progress)
        }

        // Main Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ex.ro,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SpeakerButton(onClick = { onSpeak(ex.ro) })
                    }

                    if (ex.transcription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "[${ex.transcription}]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isRevealed) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = ex.ru,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (!isRevealed) {
            Button(
                onClick = { isRevealed = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Показать перевод", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BadRedSoftLight,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable {
                            onMark(item.word.id, ex.ro, false)
                            currentIndex++
                            isRevealed = false
                        },
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Не знаю",
                            fontWeight = FontWeight.Bold,
                            color = BadRedLight,
                            fontSize = 16.sp
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = GoodGreenSoftLight,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable {
                            onMark(item.word.id, ex.ro, true)
                            currentIndex++
                            isRevealed = false
                        },
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Знаю",
                            fontWeight = FontWeight.Bold,
                            color = GoodGreenLight,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
