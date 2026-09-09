package com.aistudio.romaniandaily.data.repository

import android.content.Context
import com.aistudio.romaniandaily.data.local.*
import com.aistudio.romaniandaily.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RomanianRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.progressDao()

    private val _builtInWords = mutableListOf<Word>()
    private val _grammarRules = mutableListOf<GrammarRule>()
    private val _builtInPhrases = mutableListOf<Phrase>()
    private val _grammarQuestions = mutableListOf<GrammarQuestion>()

    private val _wordsState = MutableStateFlow<List<Word>>(emptyList())
    val wordsFlow: StateFlow<List<Word>> = _wordsState.asStateFlow()

    private val _phrasesState = MutableStateFlow<List<Phrase>>(emptyList())
    val phrasesFlow: StateFlow<List<Phrase>> = _phrasesState.asStateFlow()

    val grammarRules: List<GrammarRule> get() = _grammarRules
    val grammarQuestions: List<GrammarQuestion> get() = _grammarQuestions

    private val _learnedWordIds = MutableStateFlow<Set<String>>(emptySet())
    val learnedWordIds: StateFlow<Set<String>> = _learnedWordIds.asStateFlow()

    private val _wrongIds = MutableStateFlow<Set<String>>(emptySet())
    val wrongIds: StateFlow<Set<String>> = _wrongIds.asStateFlow()

    private val _currentDay = MutableStateFlow(1)
    val currentDay: StateFlow<Int> = _currentDay.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _attempts = MutableStateFlow(0)
    val attempts: StateFlow<Int> = _attempts.asStateFlow()

    private val _correct = MutableStateFlow(0)
    val correct: StateFlow<Int> = _correct.asStateFlow()

    private val _exampleMarks = MutableStateFlow<Map<String, String>>(emptyMap())
    val exampleMarks: StateFlow<Map<String, String>> = _exampleMarks.asStateFlow()

    private val _srsMap = MutableStateFlow<Map<String, SrsEntry>>(emptyMap())
    val srsMap: StateFlow<Map<String, SrsEntry>> = _srsMap.asStateFlow()

    private val _hardStreak = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val _studyDays = MutableStateFlow<Set<String>>(emptySet())
    val studyDays: StateFlow<Set<String>> = _studyDays.asStateFlow()

    private var lastStudyDate: String? = null

    private val srsBoxes = intArrayOf(1, 3, 7, 14, 30, 60)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun initialize() = withContext(Dispatchers.IO) {
        loadAssets()
        loadProgressFromDb()
        refreshCombinedWords()
        refreshCombinedPhrases()
    }

    private fun loadAssets() {
        if (_builtInWords.isNotEmpty()) return

        // 1. Words parts 1 to 5
        for (i in 1..5) {
            try {
                val filename = "words-part$i.json"
                val jsonStr = context.assets.open(filename).bufferedReader().use { it.readText() }
                val arr = JSONArray(jsonStr)
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    val id = obj.optString("id", "$i-$j")
                    val word = obj.optString("word", "").trim()
                    val translation = obj.optString("translation", "").trim()
                    val transcription = obj.optString("transcription", "").trim()
                    val day = obj.optInt("day", 1)

                    val examples = mutableListOf<Example>()
                    val exArr = obj.optJSONArray("examples")
                    if (exArr != null) {
                        for (k in 0 until exArr.length()) {
                            val exObj = exArr.getJSONObject(k)
                            examples.add(
                                Example(
                                    ro = exObj.optString("ro", "").trim(),
                                    ru = exObj.optString("ru", "").trim(),
                                    transcription = exObj.optString("transcription", "").trim()
                                )
                            )
                        }
                    }
                    _builtInWords.add(
                        Word(
                            id = id,
                            word = word,
                            translation = translation,
                            transcription = transcription,
                            day = day,
                            examples = examples
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Grammar rules
        try {
            val gStr = context.assets.open("grammar.json").bufferedReader().use { it.readText() }
            val gObj = JSONObject(gStr)
            val keys = gObj.keys()
            val list = mutableListOf<GrammarRule>()
            while (keys.hasNext()) {
                val key = keys.next()
                val dayNum = key.toIntOrNull() ?: 1
                val arr = gObj.getJSONArray(key)
                val title = if (arr.length() > 0) arr.getString(0) else "Правила дня $dayNum"
                val notes = mutableListOf<String>()
                for (n in 1 until arr.length()) {
                    notes.add(arr.getString(n))
                }
                list.add(GrammarRule(day = dayNum, title = title, notes = notes))
            }
            list.sortBy { it.day }
            _grammarRules.clear()
            _grammarRules.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Phrases
        try {
            val pStr = context.assets.open("phrases.json").bufferedReader().use { it.readText() }
            val pArr = JSONArray(pStr)
            val list = mutableListOf<Phrase>()
            for (i in 0 until pArr.length()) {
                val obj = pArr.getJSONObject(i)
                list.add(
                    Phrase(
                        id = obj.optString("id", "p-$i"),
                        place = obj.optString("place", "home"),
                        ro = obj.optString("ro", ""),
                        ru = obj.optString("ru", "")
                    )
                )
            }
            _builtInPhrases.clear()
            _builtInPhrases.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Quiz
        try {
            val qStr = context.assets.open("quiz.json").bufferedReader().use { it.readText() }
            val qArr = JSONArray(qStr)
            val list = mutableListOf<GrammarQuestion>()
            for (i in 0 until qArr.length()) {
                val obj = qArr.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optsArr = obj.getJSONArray("opts")
                for (o in 0 until optsArr.length()) {
                    opts.add(optsArr.getString(o))
                }
                list.add(
                    GrammarQuestion(
                        id = obj.optString("id", "q-$i"),
                        tag = obj.optString("tag", "grammar"),
                        q = obj.optString("q", ""),
                        opts = opts,
                        a = obj.optInt("a", 0),
                        why = obj.optString("why", "")
                    )
                )
            }
            _grammarQuestions.clear()
            _grammarQuestions.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadProgressFromDb() {
        val p = dao.getProgress() ?: ProgressEntity()
        _currentDay.value = p.currentDay
        _streak.value = p.streak
        lastStudyDate = p.lastStudyDate
        _attempts.value = p.attempts
        _correct.value = p.correct

        // learned
        val learnedSet = mutableSetOf<String>()
        try {
            val lArr = JSONArray(p.learnedIdsJson)
            for (i in 0 until lArr.length()) learnedSet.add(lArr.getString(i))
        } catch (_: Exception) {}
        _learnedWordIds.value = learnedSet

        // wrong
        val wrongSet = mutableSetOf<String>()
        try {
            val wArr = JSONArray(p.wrongIdsJson)
            for (i in 0 until wArr.length()) wrongSet.add(wArr.getString(i))
        } catch (_: Exception) {}
        _wrongIds.value = wrongSet

        // example marks
        val exMap = mutableMapOf<String, String>()
        try {
            val exObj = JSONObject(p.exampleMarksJson)
            val keys = exObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                exMap[k] = exObj.getString(k)
            }
        } catch (_: Exception) {}
        _exampleMarks.value = exMap

        // srs
        val sMap = mutableMapOf<String, SrsEntry>()
        try {
            val sObj = JSONObject(p.srsMapJson)
            val keys = sObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val entryObj = sObj.getJSONObject(k)
                sMap[k] = SrsEntry(
                    box = entryObj.optInt("box", 0),
                    dueDate = entryObj.optString("dueDate", "")
                )
            }
        } catch (_: Exception) {}
        _srsMap.value = sMap

        // hard streak
        val hMap = mutableMapOf<String, Int>()
        try {
            val hObj = JSONObject(p.hardStreakJson)
            val keys = hObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                hMap[k] = hObj.getInt(k)
            }
        } catch (_: Exception) {}
        _hardStreak.value = hMap

        // study days
        val days = dao.getAllStudyDays().toSet()
        _studyDays.value = days
    }

    private suspend fun persistProgress() = withContext(Dispatchers.IO) {
        val lArr = JSONArray()
        _learnedWordIds.value.forEach { lArr.put(it) }

        val wArr = JSONArray()
        _wrongIds.value.forEach { wArr.put(it) }

        val exObj = JSONObject()
        _exampleMarks.value.forEach { (k, v) -> exObj.put(k, v) }

        val sObj = JSONObject()
        _srsMap.value.forEach { (k, v) ->
            val entryObj = JSONObject()
            entryObj.put("box", v.box)
            entryObj.put("dueDate", v.dueDate)
            sObj.put(k, entryObj)
        }

        val hObj = JSONObject()
        _hardStreak.value.forEach { (k, v) -> hObj.put(k, v) }

        dao.saveProgress(
            ProgressEntity(
                id = 1,
                currentDay = _currentDay.value,
                streak = _streak.value,
                lastStudyDate = lastStudyDate,
                attempts = _attempts.value,
                correct = _correct.value,
                learnedIdsJson = lArr.toString(),
                wrongIdsJson = wArr.toString(),
                exampleMarksJson = exObj.toString(),
                srsMapJson = sObj.toString(),
                hardStreakJson = hObj.toString()
            )
        )
    }

    suspend fun refreshCombinedWords() = withContext(Dispatchers.IO) {
        val custom = dao.getCustomWords()
        if (custom.isEmpty()) {
            _wordsState.value = _builtInWords
        } else {
            val customMapped = custom.map {
                Word(
                    id = it.id,
                    word = it.word,
                    translation = it.translation,
                    transcription = it.transcription,
                    day = it.day,
                    examples = emptyList()
                )
            }
            _wordsState.value = _builtInWords + customMapped
        }
    }

    suspend fun refreshCombinedPhrases() = withContext(Dispatchers.IO) {
        // Collect custom phrases if any
        _phrasesState.value = _builtInPhrases
    }

    fun todayISO(): String {
        return isoFormat.format(Date())
    }

    private fun addDaysISO(baseIso: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            cal.time = isoFormat.parse(baseIso) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            isoFormat.format(cal.time)
        } catch (_: Exception) {
            todayISO()
        }
    }

    fun dueSrsWords(): List<Word> {
        val today = todayISO()
        val srs = _srsMap.value
        return _wordsState.value.filter { w ->
            val entry = srs[w.id]
            entry != null && entry.dueDate.isNotEmpty() && entry.dueDate <= today
        }
    }

    suspend fun markResult(wordId: String, isOk: Boolean) {
        _attempts.value += 1
        if (isOk) _correct.value += 1

        val currentLearned = _learnedWordIds.value.toMutableSet()
        val currentWrong = _wrongIds.value.toMutableSet()
        val currentHard = _hardStreak.value.toMutableMap()
        val currentSrs = _srsMap.value.toMutableMap()

        if (isOk) {
            currentLearned.add(wordId)
            val count = (currentHard[wordId] ?: 0) + 1
            currentHard[wordId] = count
            if (count >= 3) {
                currentWrong.remove(wordId)
                currentHard.remove(wordId)
            }
        } else {
            currentWrong.add(wordId)
            currentHard[wordId] = 0
        }

        // Apply SRS Leitner box calculation
        val entry = currentSrs[wordId] ?: SrsEntry(box = 0, dueDate = todayISO())
        val newBox = if (isOk) {
            (entry.box + 1).coerceAtMost(srsBoxes.size)
        } else {
            (entry.box - 1).coerceAtLeast(0)
        }
        val intervalDays = if (newBox == 0) 0 else srsBoxes[newBox - 1]
        val newDueDate = addDaysISO(todayISO(), intervalDays)
        currentSrs[wordId] = SrsEntry(box = newBox, dueDate = newDueDate)

        _learnedWordIds.value = currentLearned
        _wrongIds.value = currentWrong
        _hardStreak.value = currentHard
        _srsMap.value = currentSrs

        recordTodayStudy()
        persistProgress()
    }

    suspend fun toggleLearned(wordId: String) {
        val set = _learnedWordIds.value.toMutableSet()
        if (set.contains(wordId)) {
            set.remove(wordId)
        } else {
            set.add(wordId)
        }
        _learnedWordIds.value = set
        persistProgress()
    }

    suspend fun markExample(wordId: String, ro: String, know: Boolean) {
        val key = "$wordId::$ro"
        val map = _exampleMarks.value.toMutableMap()
        map[key] = if (know) "know" else "unknow"
        _exampleMarks.value = map
        persistProgress()
    }

    fun exampleStatus(wordId: String, ro: String): String? {
        return _exampleMarks.value["$wordId::$ro"]
    }

    suspend fun recordTodayStudy() {
        val today = todayISO()
        if (lastStudyDate != today) {
            val calToday = Calendar.getInstance()
            val isConsecutive = lastStudyDate?.let { last ->
                val calLast = Calendar.getInstance()
                isoFormat.parse(last)?.let { calLast.time = it }
                calLast.add(Calendar.DAY_OF_YEAR, 1)
                isoFormat.format(calLast.time) == today
            } ?: false

            if (isConsecutive) {
                _streak.value += 1
            } else if (lastStudyDate == null) {
                _streak.value = 1
            } else {
                _streak.value = 1
            }
            lastStudyDate = today
        }
        val set = _studyDays.value.toMutableSet()
        if (!set.contains(today)) {
            set.add(today)
            _studyDays.value = set
            withContext(Dispatchers.IO) {
                dao.insertStudyDay(StudyDayEntity(today))
            }
        }
        persistProgress()
    }

    suspend fun setDay(day: Int) {
        _currentDay.value = day
        persistProgress()
    }

    suspend fun advanceDay() {
        _currentDay.value += 1
        recordTodayStudy()
        persistProgress()
    }

    suspend fun importCustomWords(text: String, replace: Boolean): Int = withContext(Dispatchers.IO) {
        val lines = text.lines()
        val newWords = mutableListOf<CustomWordEntity>()
        var dayCursor = if (replace) 1 else ((_wordsState.value.maxOfOrNull { it.day } ?: 1) + 1)
        var countInDay = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split("—", "–", "-")
            if (parts.size >= 2) {
                val wordRo = parts[0].trim()
                var rest = parts[1].trim()
                var transcription = ""
                val trMatch = Regex("\\[(.*?)\\]").find(rest)
                if (trMatch != null) {
                    transcription = trMatch.groupValues[1]
                    rest = rest.replace(trMatch.value, "").trim()
                }
                if (wordRo.isNotEmpty() && rest.isNotEmpty()) {
                    newWords.add(
                        CustomWordEntity(
                            id = "custom-${System.currentTimeMillis()}-${newWords.size}",
                            word = wordRo,
                            translation = rest,
                            transcription = transcription,
                            day = dayCursor
                        )
                    )
                    countInDay++
                    if (countInDay >= 10) {
                        dayCursor++
                        countInDay = 0
                    }
                }
            }
        }

        if (replace) {
            dao.clearCustomWords()
            _currentDay.value = 1
            _learnedWordIds.value = emptySet()
            _wrongIds.value = emptySet()
        }
        dao.insertCustomWords(newWords)
        refreshCombinedWords()
        persistProgress()
        newWords.size
    }

    suspend fun resetToDefaultCourse() = withContext(Dispatchers.IO) {
        dao.clearCustomWords()
        _currentDay.value = 1
        _learnedWordIds.value = emptySet()
        _wrongIds.value = emptySet()
        _exampleMarks.value = emptyMap()
        _srsMap.value = emptyMap()
        _hardStreak.value = emptyMap()
        refreshCombinedWords()
        persistProgress()
    }
}
