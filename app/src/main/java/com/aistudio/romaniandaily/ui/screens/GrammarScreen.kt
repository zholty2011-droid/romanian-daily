package com.aistudio.romaniandaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.GrammarQuestion
import com.aistudio.romaniandaily.data.model.GrammarRule
import com.aistudio.romaniandaily.ui.components.ProgressBar
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun GrammarRulesOverviewScreen(
    rules: List<GrammarRule>,
    onStartQuiz: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBackButton(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Грамматика",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = onStartQuiz,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Тест", fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rules) { rule ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ДЕНЬ ${rule.day}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rule.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        rule.notes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarQuizScreen(
    questions: List<GrammarQuestion>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Вопросов по грамматике нет.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Назад") }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOpt by remember { mutableStateOf<Int?>(null) }
    var correctAnswers by remember { mutableIntStateOf(0) }

    if (currentIndex >= questions.size) {
        val pct = (100 * correctAnswers) / questions.size
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Тест по грамматике",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "$correctAnswers/${questions.size} • $pct%",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(28.dp))
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
        return
    }

    val q = questions[currentIndex]
    val progress = (currentIndex + 1).toFloat() / questions.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Грамматика • ${currentIndex + 1}/${questions.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProgressBar(progress = progress)

        // Question card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = q.q,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.opts.forEachIndexed { idx, opt ->
                val isPicked = selectedOpt == idx
                val isCorrect = idx == q.a
                val (bgColor, textColor) = when {
                    selectedOpt == null -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                    isCorrect -> GoodGreenSoftLight to GoodGreenLight
                    isPicked -> BadRedSoftLight to BadRedLight
                    else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedOpt == null) {
                            selectedOpt = idx
                            if (idx == q.a) correctAnswers++
                        },
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Explanation if answered
        if (selectedOpt != null && q.why.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ОБЪЯСНЕНИЕ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = q.why,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedOpt != null) {
            Button(
                onClick = {
                    currentIndex++
                    selectedOpt = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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

@Composable
fun DailyGrammarStepScreen(
    currentDay: Int,
    rule: GrammarRule?,
    questions: List<GrammarQuestion>,
    onFinishStep: () -> Unit,
    onBack: () -> Unit,
    finishButtonText: String = "Далее: Предложения",
    modifier: Modifier = Modifier
) {
    var showingRule by remember { mutableStateOf(rule != null) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedOpt by remember { mutableStateOf<Int?>(null) }

    if (showingRule && rule != null) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "День $currentDay • Шаг 3/4: Грамматика",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "ТЕОРИЯ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                ProgressBar(progress = 0.5f)
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ПРАВИЛО ДНЯ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    rule.notes.forEach { note ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (questions.isNotEmpty()) {
                        showingRule = false
                    } else {
                        onFinishStep()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("grammar_continue_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (questions.isNotEmpty()) "Понятно • К упражнению" else finishButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    // Questions phase
    if (questions.isEmpty() || questionIndex >= questions.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Грамматика освоена!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Теория дня и проверочные задания пройдены.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onFinishStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("next_sentences_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(finishButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        return
    }

    val q = questions[questionIndex]
    val isLastQuestion = questionIndex == questions.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TopBackButton(onBack = {
                if (rule != null) showingRule = true else onBack()
            })

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "День $currentDay • Шаг 3/4: Грамматика",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Вопрос ${questionIndex + 1}/${questions.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            ProgressBar(progress = (questionIndex + 1).toFloat() / questions.size.toFloat())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Question card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = q.q,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                q.opts.forEachIndexed { idx, opt ->
                    val isPicked = selectedOpt == idx
                    val isCorrect = idx == q.a
                    val (bgColor, textColor) = when {
                        selectedOpt == null -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                        isCorrect -> GoodGreenSoftLight to GoodGreenLight
                        isPicked -> BadRedSoftLight to BadRedLight
                        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedOpt == null) {
                                selectedOpt = idx
                            },
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = textColor,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Explanation if answered
            if (selectedOpt != null && q.why.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ОБЪЯСНЕНИЕ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.why,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (selectedOpt != null) {
            Button(
                onClick = {
                    if (isLastQuestion) {
                        onFinishStep()
                    } else {
                        questionIndex++
                        selectedOpt = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isLastQuestion) "Далее: Предложения" else "Следующий вопрос",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(52.dp))
        }
    }
}
