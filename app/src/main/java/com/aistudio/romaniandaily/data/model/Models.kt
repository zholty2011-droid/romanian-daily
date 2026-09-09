package com.aistudio.romaniandaily.data.model

data class Example(
    val ro: String,
    val ru: String,
    val transcription: String = ""
)

data class Word(
    val id: String,
    val word: String,
    val translation: String,
    val transcription: String = "",
    val day: Int = 1,
    val examples: List<Example> = emptyList()
)

data class Phrase(
    val id: String,
    val place: String, // "home", "out", or "custom"
    val ro: String,
    val ru: String
)

data class GrammarRule(
    val day: Int,
    val title: String,
    val notes: List<String>
)

data class GrammarQuestion(
    val id: String,
    val tag: String,
    val q: String,
    val opts: List<String>,
    val a: Int, // index of correct answer
    val why: String
)

data class SrsEntry(
    val box: Int = 0,
    val dueDate: String = "" // YYYY-MM-DD
)

data class QuizItem(
    val word: Word,
    val options: List<Word> = emptyList(),
    var pickedId: String? = null,
    var isCorrect: Boolean? = null
)

data class WriteItem(
    val word: Word,
    var userAnswer: String = "",
    var isChecked: Boolean? = null,
    var hintLevel: Int = 0,
    var bankLetters: List<String> = emptyList()
)

data class ExampleItem(
    val word: Word,
    val example: Example,
    var userChoice: String? = null
)
