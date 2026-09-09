package com.aistudio.romaniandaily.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.data.model.*
import com.aistudio.romaniandaily.data.repository.CitizenshipData
import com.aistudio.romaniandaily.data.repository.RomanianRepository
import com.aistudio.romaniandaily.ui.components.AppBottomNavigation
import com.aistudio.romaniandaily.ui.components.AppHeader
import com.aistudio.romaniandaily.ui.screens.*
import com.aistudio.romaniandaily.ui.theme.RomanianDailyTheme
import com.aistudio.romaniandaily.util.TtsManager
import kotlinx.coroutines.launch

@Composable
fun RomanianDailyApp(
    repository: RomanianRepository,
    ttsManager: TtsManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var isDarkTheme by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("home") }

    // Repository states
    val words by repository.wordsFlow.collectAsState()
    val phrases by repository.phrasesFlow.collectAsState()
    val learnedIds by repository.learnedWordIds.collectAsState()
    val wrongIds by repository.wrongIds.collectAsState()
    val currentDay by repository.currentDay.collectAsState()
    val streak by repository.streak.collectAsState()
    val attempts by repository.attempts.collectAsState()
    val correct by repository.correct.collectAsState()
    val studyDays by repository.studyDays.collectAsState()

    val dueSrsCount = remember(words, repository.dueSrsWords()) {
        repository.dueSrsWords().size
    }

    val allDays = remember(words) {
        words.map { it.day }.distinct().sorted().ifEmpty { listOf(1) }
    }

    val dailyWords = remember(words, currentDay) {
        words.filter { it.day == currentDay }.ifEmpty { words.take(10) }
    }

    // Dynamic practice state
    var quizItems by remember { mutableStateOf<List<QuizItem>>(emptyList()) }
    var isQuizRoToRu by remember { mutableStateOf(true) }
    var isExamMode by remember { mutableStateOf(false) }

    var writeWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var isWriteRussian by remember { mutableStateOf(false) }

    var cardsWords by remember { mutableStateOf<List<Word>>(emptyList()) }

    var exampleItems by remember { mutableStateOf<List<ExampleItem>>(emptyList()) }
    var examplesTitle by remember { mutableStateOf("Примеры дня") }

    var customStudyPhrases by remember { mutableStateOf<List<Phrase>>(emptyList()) }

    // Comprehensive Daily lesson 7-step flow:
    // 1=Words, 2=Write, 3=Grammar, 4=Listening, 5=Sentence Builder, 6=Dialog, 7=Sentences/Examples
    var dailyStep by remember { mutableIntStateOf(1) }
    var showDayFinishedDialog by remember { mutableStateOf(false) }

    // Handle system back navigation to navigate back within the app instead of closing
    BackHandler(enabled = currentScreen != "home") {
        when (currentScreen) {
            "daily_flow" -> {
                if (dailyStep > 1) {
                    dailyStep--
                } else {
                    currentScreen = "home"
                }
            }
            "cards", "quiz", "write", "examples", "listening", "sentence_builder", "sprint", "grammar_overview", "grammar_quiz" -> {
                currentScreen = "practice"
            }
            "phrases_study" -> {
                currentScreen = "phrases"
            }
            else -> {
                currentScreen = "home"
            }
        }
    }

    fun startDailyFlow() {
        dailyStep = 1
        currentScreen = "daily_flow"
    }

    fun generateQuizItems(sourceWords: List<Word>, isRoToRu: Boolean, count: Int, choicesCount: Int): List<QuizItem> {
        val pool = if (sourceWords.isNotEmpty()) sourceWords else words
        val shuffledPool = pool.shuffled().take(count)
        return shuffledPool.map { targetWord ->
            val otherChoices = words.filter { it.id != targetWord.id }.shuffled().take(choicesCount - 1)
            val options = (otherChoices + targetWord).shuffled()
            QuizItem(word = targetWord, options = options)
        }
    }

    fun startQuiz(sourceWords: List<Word>, isRoToRu: Boolean, count: Int, choicesCount: Int, isExam: Boolean = false) {
        isQuizRoToRu = isRoToRu
        isExamMode = isExam
        quizItems = generateQuizItems(sourceWords, isRoToRu, count, choicesCount)
        currentScreen = "quiz"
    }

    RomanianDailyTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Persistent Top Header
                AppHeader(
                    streak = streak,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onTitleClick = { currentScreen = "home" }
                )

                // Screen body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentScreen) {
                        "home" -> {
                            HomeScreen(
                                currentDay = currentDay,
                                dailyWords = dailyWords,
                                allWordsCount = words.size,
                                learnedCount = learnedIds.size,
                                dueSrsCount = dueSrsCount,
                                allDays = allDays,
                                onSelectDay = { day ->
                                    coroutineScope.launch { repository.setDay(day) }
                                },
                                onStartDay = { startDailyFlow() },
                                onNavigate = { screen -> currentScreen = screen }
                            )
                        }

                        "daily_flow" -> {
                            when (dailyStep) {
                                1 -> {
                                    LessonScreen(
                                        words = dailyWords,
                                        stepTitle = "День $currentDay • Этап 1/7: Новые слова",
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        onMarkWord = { wordId, isKnown ->
                                            coroutineScope.launch { repository.markResult(wordId, isKnown) }
                                        },
                                        onFinishStep = {
                                            dailyStep = 2
                                        },
                                        onBack = { currentScreen = "home" }
                                    )
                                }
                                2 -> {
                                    WriteScreen(
                                        words = dailyWords,
                                        isWriteRussian = false,
                                        stepTitle = "День $currentDay • Этап 2/7: Правописание",
                                        finishButtonText = "Далее: Грамматика",
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        onAnswer = { wordId, isCorrect ->
                                            coroutineScope.launch { repository.markResult(wordId, isCorrect) }
                                        },
                                        onFinish = {
                                            dailyStep = 3
                                        },
                                        onBack = { dailyStep = 1 }
                                    )
                                }
                                3 -> {
                                    val rule = repository.grammarRules.find { it.day == currentDay }
                                        ?: repository.grammarRules.firstOrNull()

                                    val tagForDay = when (currentDay) {
                                        1 -> "polite"
                                        2 -> "time"
                                        3 -> "art"
                                        4 -> "dir"
                                        5 -> "dir"
                                        6 -> "prep"
                                        7 -> "time"
                                        9 -> "fi"
                                        10 -> "shop"
                                        11 -> "cafe"
                                        12 -> "feel"
                                        17 -> "îâ"
                                        18 -> "polite"
                                        19 -> "neg"
                                        20 -> "să"
                                        24 -> "poss"
                                        31 -> "polite"
                                        33 -> "pron"
                                        34 -> "time"
                                        35 -> "fi"
                                        37 -> "prep"
                                        38 -> "feel"
                                        else -> null
                                    }

                                    val dayQuestions = if (tagForDay != null) {
                                        repository.grammarQuestions.filter { it.tag == tagForDay }.ifEmpty {
                                            repository.grammarQuestions.take(3)
                                        }
                                    } else {
                                        val offset = ((currentDay - 1) * 2) % repository.grammarQuestions.size.coerceAtLeast(1)
                                        repository.grammarQuestions.drop(offset).take(3).ifEmpty {
                                            repository.grammarQuestions.take(3)
                                        }
                                    }

                                    DailyGrammarStepScreen(
                                        currentDay = currentDay,
                                        rule = rule,
                                        questions = dayQuestions,
                                        finishButtonText = "Далее: Аудирование",
                                        onFinishStep = {
                                            dailyStep = 4
                                        },
                                        onBack = { dailyStep = 2 }
                                    )
                                }
                                4 -> {
                                    // Step 4: Listening comprehension
                                    ListeningScreen(
                                        wordsPool = (dailyWords + words.take(15)).distinctBy { it.id },
                                        maxQuestions = 6,
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        onAnswer = { wordId, isCorrect ->
                                            coroutineScope.launch { repository.markResult(wordId, isCorrect) }
                                        },
                                        finishButtonText = "Далее: Конструктор фраз",
                                        onFinish = {
                                            dailyStep = 5
                                        },
                                        onBack = { dailyStep = 3 }
                                    )
                                }
                                5 -> {
                                    // Step 5: Sentence Builder
                                    SentenceBuilderScreen(
                                        wordsPool = dailyWords.ifEmpty { words.take(10) },
                                        maxPuzzles = 5,
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        finishButtonText = "Далее: Живой диалог",
                                        onFinish = {
                                            dailyStep = 6
                                        },
                                        onBack = { dailyStep = 4 }
                                    )
                                }
                                6 -> {
                                    // Step 6: Dialog of the day
                                    val scenarioIndex = ((currentDay - 1) % CitizenshipData.dialogs.size.coerceAtLeast(1))
                                    val scenario = CitizenshipData.dialogs.getOrNull(scenarioIndex)
                                        ?: CitizenshipData.dialogs.first()

                                    DialogsScreen(
                                        initialScenarioId = scenario.id,
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        finishButtonText = "Далее: Разбор предложений",
                                        onFinish = {
                                            dailyStep = 7
                                        },
                                        onBack = { dailyStep = 5 }
                                    )
                                }
                                7 -> {
                                    // Step 7: Final Daily Examples & Sentences
                                    val dailyExamples = dailyWords.flatMap { w ->
                                        w.examples.map { ex -> ExampleItem(word = w, example = ex) }
                                    }.ifEmpty {
                                        phrases.take(6).map { p ->
                                            ExampleItem(
                                                word = Word(id = p.id, word = p.ro, translation = p.ru),
                                                example = Example(ro = p.ro, ru = p.ru)
                                            )
                                        }
                                    }

                                    ExamplesScreen(
                                        items = dailyExamples,
                                        title = "День $currentDay • Этап 7/7: Предложения",
                                        finishButtonText = "Завершить день 🎉",
                                        onSpeak = { text -> ttsManager.speak(text) },
                                        onMark = { wordId, exRo, isKnown ->
                                            coroutineScope.launch { repository.markExample(wordId, exRo, isKnown) }
                                        },
                                        onFinish = {
                                            coroutineScope.launch {
                                                repository.advanceDay()
                                            }
                                            showDayFinishedDialog = true
                                            currentScreen = "home"
                                        },
                                        onBack = { dailyStep = 6 }
                                    )
                                }
                            }
                        }

                        "practice" -> {
                            val unknownCount = words.sumOf { w ->
                                w.examples.count { repository.exampleStatus(w.id, it.ro) == "unknow" }
                            }

                            PracticeScreen(
                                currentDay = currentDay,
                                wrongCount = wrongIds.size,
                                unknownExamplesCount = unknownCount,
                                onStartTestDay = { roToRu, cnt, opts ->
                                    startQuiz(dailyWords, roToRu, cnt, opts)
                                },
                                onStartTestAllDays = { roToRu, cnt, opts ->
                                    val unlocked = words.filter { it.day <= currentDay }
                                    startQuiz(unlocked, roToRu, cnt, opts)
                                },
                                onStartCardsDay = {
                                    cardsWords = dailyWords
                                    currentScreen = "cards"
                                },
                                onStartExamplesDay = { onlyUnknown ->
                                    val list = mutableListOf<ExampleItem>()
                                    val targetPool = if (onlyUnknown) words else dailyWords
                                    targetPool.forEach { w ->
                                        w.examples.forEach { ex ->
                                            val status = repository.exampleStatus(w.id, ex.ro)
                                            if (!onlyUnknown || status == "unknow") {
                                                list.add(ExampleItem(word = w, example = ex))
                                            }
                                        }
                                    }
                                    exampleItems = list
                                    examplesTitle = if (onlyUnknown) "Примеры, которые не помню" else "Примеры дня"
                                    currentScreen = "examples"
                                },
                                onStartWrite = { isRussian ->
                                    writeWords = dailyWords
                                    isWriteRussian = isRussian
                                    currentScreen = "write"
                                },
                                onStartListening = {
                                    currentScreen = "listening"
                                },
                                onStartSentenceBuilder = {
                                    currentScreen = "sentence_builder"
                                },
                                onStartSprint = {
                                    currentScreen = "sprint"
                                },
                                onStartCitizenship = {
                                    currentScreen = "citizenship"
                                },
                                onStartDialogs = {
                                    currentScreen = "dialogs"
                                },
                                onStartGrammarQuiz = {
                                    currentScreen = "grammar_quiz"
                                },
                                onStartGrammarOverview = {
                                    currentScreen = "grammar_overview"
                                },
                                onStartPhrases = {
                                    currentScreen = "phrases"
                                },
                                onStartExam = {
                                    startQuiz(words, isRoToRu = true, count = 20, choicesCount = 4, isExam = true)
                                },
                                onRetryWrong = {
                                    val wrongWords = words.filter { wrongIds.contains(it.id) }
                                    startQuiz(wrongWords, isRoToRu = true, count = wrongWords.size, choicesCount = 4)
                                }
                            )
                        }

                        "quiz" -> {
                            QuizScreen(
                                items = quizItems,
                                isRoToRu = isQuizRoToRu,
                                isExamMode = isExamMode,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onAnswer = { _, wordId, isCorrect ->
                                    coroutineScope.launch { repository.markResult(wordId, isCorrect) }
                                },
                                onRetryWrong = {
                                    val failedWords = quizItems.filter { it.isCorrect == false }.map { it.word }
                                    if (failedWords.isNotEmpty()) {
                                        startQuiz(failedWords, isQuizRoToRu, failedWords.size, 4)
                                    }
                                },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "cards" -> {
                            CardsScreen(
                                words = cardsWords,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onAnswer = { wordId, isKnown ->
                                    coroutineScope.launch { repository.markResult(wordId, isKnown) }
                                },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "write" -> {
                            WriteScreen(
                                words = writeWords,
                                isWriteRussian = isWriteRussian,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onAnswer = { wordId, isCorrect ->
                                    coroutineScope.launch { repository.markResult(wordId, isCorrect) }
                                },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "examples" -> {
                            ExamplesScreen(
                                items = exampleItems,
                                title = examplesTitle,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onMark = { wordId, exRo, isKnown ->
                                    coroutineScope.launch { repository.markExample(wordId, exRo, isKnown) }
                                },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "grammar_overview" -> {
                            GrammarRulesOverviewScreen(
                                rules = repository.grammarRules,
                                onStartQuiz = { currentScreen = "grammar_quiz" },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "grammar_quiz" -> {
                            GrammarQuizScreen(
                                questions = repository.grammarQuestions,
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "phrases" -> {
                            PhrasesScreen(
                                phrases = phrases,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onStudyPhrases = { selPhrases ->
                                    customStudyPhrases = selPhrases
                                    currentScreen = "phrases_study"
                                },
                                onWritePhrase = { phrase ->
                                    // can practice
                                },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "phrases_study" -> {
                            PhrasesStudyScreen(
                                phrases = customStudyPhrases.ifEmpty { phrases.take(10) },
                                onSpeak = { text -> ttsManager.speak(text) },
                                onFinish = { currentScreen = "phrases" },
                                onBack = { currentScreen = "phrases" }
                            )
                        }

                        "dictionary" -> {
                            DictionaryScreen(
                                words = words,
                                learnedIds = learnedIds,
                                wrongIds = wrongIds,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onToggleLearned = { wordId ->
                                    coroutineScope.launch { repository.toggleLearned(wordId) }
                                },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "stats" -> {
                            StatsScreen(
                                totalWords = words.size,
                                learnedCount = learnedIds.size,
                                attempts = attempts,
                                correct = correct,
                                streak = streak,
                                dueSrsCount = dueSrsCount,
                                wrongCount = wrongIds.size,
                                studyDays = studyDays,
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "settings" -> {
                            SettingsScreen(
                                currentSpeechRate = ttsManager.getRate(),
                                isDarkTheme = isDarkTheme,
                                onSpeechRateChange = { rate -> ttsManager.setRate(rate) },
                                onTestSound = { ttsManager.speak("Bună ziua, cum ești?") },
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                onResetProgress = {
                                    coroutineScope.launch { repository.resetToDefaultCourse() }
                                },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "import" -> {
                            CustomWordsScreen(
                                onImport = { text, replace ->
                                    coroutineScope.launch { repository.importCustomWords(text, replace) }
                                },
                                onResetToDefault = {
                                    coroutineScope.launch { repository.resetToDefaultCourse() }
                                },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "citizenship" -> {
                            CitizenshipScreen(
                                onSpeak = { text -> ttsManager.speak(text) },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "dialogs" -> {
                            DialogsScreen(
                                onSpeak = { text -> ttsManager.speak(text) },
                                onBack = { currentScreen = "home" }
                            )
                        }

                        "listening" -> {
                            ListeningScreen(
                                wordsPool = words,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onAnswer = { wordId, isCorrect ->
                                    coroutineScope.launch { repository.markResult(wordId, isCorrect) }
                                },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "sentence_builder" -> {
                            SentenceBuilderScreen(
                                wordsPool = words,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onBack = { currentScreen = "practice" }
                            )
                        }

                        "sprint" -> {
                            SprintScreen(
                                wordsPool = words,
                                onSpeak = { text -> ttsManager.speak(text) },
                                onFinish = { /* handled in screen */ },
                                onBack = { currentScreen = "practice" }
                            )
                        }
                    }
                }

                // Persistent Bottom Nav Bar (shown on primary tabs)
                val isTabScreen = currentScreen in listOf("home", "practice", "dictionary")
                if (isTabScreen) {
                    AppBottomNavigation(
                        currentRoute = currentScreen,
                        onNavigate = { route -> currentScreen = route },
                        onStartDaily = { startDailyFlow() }
                    )
                }
            }

            // Day Finished Celebration Dialog
            if (showDayFinishedDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDayFinishedDialog = false
                        currentScreen = "home"
                    },
                    title = {
                        Text("День успешно завершён!", style = MaterialTheme.typography.headlineMedium)
                    },
                    text = {
                        Column {
                            Text("Отличная работа! Ударный режим: $streak дней 🔥.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Вы продвинулись к следующему дню курса.")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDayFinishedDialog = false
                                currentScreen = "home"
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Продолжить", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PhrasesStudyScreen(
    phrases: List<Phrase>,
    onSpeak: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (phrases.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = onBack) { Text("Назад") }
        }
        return
    }

    var idx by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    if (idx >= phrases.size) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Фразы пройдены!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onFinish) { Text("Завершить") }
        }
        return
    }

    val phrase = phrases[idx]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Назад") }
            Text(text = "Фраза ${idx + 1}/${phrases.size}", fontWeight = FontWeight.Bold)
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = phrase.ro,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 30.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                IconButton(onClick = { onSpeak(phrase.ro) }) {
                    Text("🔊", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                if (isRevealed) {
                    Text(
                        text = phrase.ru,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!isRevealed) {
            Button(
                onClick = { isRevealed = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Показать перевод", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = {
                    idx++
                    isRevealed = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (idx == phrases.size - 1) "Завершить" else "Далее", fontWeight = FontWeight.Bold)
            }
        }
    }
}
