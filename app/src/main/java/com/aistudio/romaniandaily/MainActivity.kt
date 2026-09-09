package com.aistudio.romaniandaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aistudio.romaniandaily.data.repository.RomanianRepository
import com.aistudio.romaniandaily.ui.RomanianDailyApp
import com.aistudio.romaniandaily.ui.theme.RomanianDailyTheme
import com.aistudio.romaniandaily.util.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var repository: RomanianRepository
    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = RomanianRepository(this)
        ttsManager = TtsManager(this)

        setContent {
            var isReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    repository.initialize()
                }
                isReady = true
            }

            RomanianDailyTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    if (isReady) {
                        RomanianDailyApp(
                            repository = repository,
                            ttsManager = ttsManager,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
