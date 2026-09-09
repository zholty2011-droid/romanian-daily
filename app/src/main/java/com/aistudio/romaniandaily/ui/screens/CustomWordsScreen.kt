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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.romaniandaily.ui.components.TopBackButton
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun CustomWordsScreen(
    onImport: (text: String, replace: Boolean) -> Unit,
    onResetToDefault: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var isReplace by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBackButton(onBack = onBack)

        Text(
            text = "Свои слова",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ФОРМАТ ВВОДА",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Каждое слово с новой строки в формате:\ncuvânt — перевод [транскрипция]\nили просто: cuvânt — перевод",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                message = null
            },
            placeholder = {
                Text("bună — привет [бунэ]\nmultumesc — спасибо [мульцумеск]\nla revedere — до свидания")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("custom_words_input"),
            shape = RoundedCornerShape(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!isReplace) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isReplace = false }
            ) {
                Text(
                    text = "Добавить к курсу",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isReplace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isReplace) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isReplace = true }
            ) {
                Text(
                    text = "Заменить курс",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReplace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                )
            }
        }

        if (message != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoodGreenSoftLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message ?: "",
                    color = GoodGreenLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    onImport(textInput, isReplace)
                    message = "Слова успешно импортированы!"
                    textInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_import_words"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Импортировать слова", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                onResetToDefault()
                message = "Курс возвращен к стандартной программе."
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_reset_default_course"),
            shape = CircleShape
        ) {
            Text("Вернуть стандартный курс", fontWeight = FontWeight.Bold)
        }
    }
}
