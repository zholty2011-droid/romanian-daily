package com.aistudio.romaniandaily.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentDay: Int = 1,
    val streak: Int = 0,
    val lastStudyDate: String? = null,
    val attempts: Int = 0,
    val correct: Int = 0,
    val learnedIdsJson: String = "[]",
    val wrongIdsJson: String = "[]",
    val exampleMarksJson: String = "{}",
    val srsMapJson: String = "{}",
    val hardStreakJson: String = "{}"
)

@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey val id: String,
    val word: String,
    val translation: String,
    val transcription: String = "",
    val day: Int = 1
)

@Entity(tableName = "study_days")
data class StudyDayEntity(
    @PrimaryKey val dateIso: String
)

@Entity(tableName = "custom_phrases")
data class CustomPhraseEntity(
    @PrimaryKey val id: String,
    val place: String,
    val ro: String,
    val ru: String
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getProgressFlow(): Flow<ProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getProgress(): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ProgressEntity)

    @Query("SELECT dateIso FROM study_days")
    fun getAllStudyDaysFlow(): Flow<List<String>>

    @Query("SELECT dateIso FROM study_days")
    suspend fun getAllStudyDays(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudyDay(day: StudyDayEntity)

    @Query("SELECT * FROM custom_words")
    fun getCustomWordsFlow(): Flow<List<CustomWordEntity>>

    @Query("SELECT * FROM custom_words")
    suspend fun getCustomWords(): List<CustomWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomWords(words: List<CustomWordEntity>)

    @Query("DELETE FROM custom_words")
    suspend fun clearCustomWords()

    @Query("SELECT * FROM custom_phrases")
    fun getCustomPhrasesFlow(): Flow<List<CustomPhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPhrase(phrase: CustomPhraseEntity)
}
