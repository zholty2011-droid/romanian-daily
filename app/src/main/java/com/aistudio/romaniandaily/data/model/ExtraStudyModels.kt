package com.aistudio.romaniandaily.data.model

data class CitizenshipTopic(
    val id: String,
    val titleRu: String,
    val titleRo: String,
    val icon: String,
    val questions: List<CitizenshipQA>
)

data class CitizenshipQA(
    val id: String,
    val qRo: String,
    val qRu: String,
    val qTrans: String = "",
    val aRo: String,
    val aRu: String,
    val aTrans: String = "",
    val tip: String = ""
)

data class DialogScenario(
    val id: String,
    val titleRu: String,
    val titleRo: String,
    val icon: String,
    val description: String,
    val lines: List<DialogLine>
)

data class DialogLine(
    val speaker: String, // e.g. "Ofițer", "Vânzător", "Dumneavoastră"
    val isUser: Boolean,
    val ro: String,
    val ru: String,
    val transcription: String = ""
)

data class WordBlockItem(
    val targetSentenceRo: String,
    val translationRu: String,
    val tokens: List<String>,
    val distractors: List<String> = emptyList()
)

data class ListeningItem(
    val id: String,
    val phraseRo: String,
    val phraseRu: String,
    val optionsRu: List<String>,
    val correctIndex: Int
)
