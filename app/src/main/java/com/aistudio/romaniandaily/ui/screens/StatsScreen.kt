package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    totalWords: Int,
    learnedCount: Int,
    attempts: Int,
    correct: Int,
    streak: Int,
    dueSrsCount: Int,
    wrongCount: Int,
    studyDays: Set<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val accuracy = if (attempts > 0) (100 * correct) / attempts else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Text(
            text = "Статистика",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Streak Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "УДАРНЫЙ РЕЖИМ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$streak ${if (streak == 1) "день" else "дней"} подряд",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(text = "🔥", fontSize = 36.sp)
            }
        }

        // 3 Key Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricTile(
                title = "Выучено",
                value = "$learnedCount",
                modifier = Modifier.weight(1f)
            )
            StatMetricTile(
                title = "Всего",
                value = "$totalWords",
                modifier = Modifier.weight(1f)
            )
            StatMetricTile(
                title = "Точность",
                value = "$accuracy%",
                modifier = Modifier.weight(1f)
            )
        }

        // Repetition System Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ИНТЕРВАЛЫ ПОВТОРЕНИЙ (SRS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "1 → 3 → 7 → 14 → 30 → 60 дней",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Сегодня к повторению: $dueSrsCount слов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dueSrsCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                if (wrongCount > 0) {
                    Text(
                        text = "Сложных слов: $wrongCount (снимаются после 3 верных ответов подряд)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BadRedLight
                    )
                }
            }
        }

        // 8-Week Activity Calendar Grid
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "КАЛЕНДАРЬ ЗА 8 НЕДЕЛЬ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Build 8 weeks calendar grid (56 days)
                CalendarHeatmap(studyDays = studyDays)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatMetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CalendarHeatmap(studyDays: Set<String>) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = Calendar.getInstance()

    // 8 columns (weeks), each column has 7 days (Mon..Sun)
    val weeks = remember(studyDays) {
        val list = mutableListOf<List<String>>()
        val cal = Calendar.getInstance()
        // align to end of current week
        val currentDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=Mon, 6=Sun
        cal.add(Calendar.DAY_OF_YEAR, -((7 * 7) + currentDayOfWeek)) // 8 weeks back from Monday

        for (w in 0 until 8) {
            val weekDays = mutableListOf<String>()
            for (d in 0 until 7) {
                weekDays.add(sdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            list.add(weekDays)
        }
        list
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { dateIso ->
                    val isStudied = studyDays.contains(dateIso)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isStudied) GoodGreenLight
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}
