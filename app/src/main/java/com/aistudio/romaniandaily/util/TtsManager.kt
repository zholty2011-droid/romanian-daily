package com.aistudio.romaniandaily.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var speechRate = 0.85f

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ro", "RO"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("ro"))
            }
            tts?.setSpeechRate(speechRate)
            isReady = true
        } else {
            Log.e("TtsManager", "TTS initialization failed")
        }
    }

    fun setRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 1.5f)
        if (isReady) {
            tts?.setSpeechRate(speechRate)
        }
    }

    fun getRate(): Float = speechRate

    fun speak(text: String) {
        if (!isReady || text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RomanianDailyUtterance")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
