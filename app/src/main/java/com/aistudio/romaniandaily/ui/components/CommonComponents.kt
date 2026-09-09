package com.aistudio.romaniandaily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.aistudio.romaniandaily.ui.theme.*

@Composable
fun AppHeader(
    streak: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clickable(onClick = onTitleClick)
                    .testTag("header_brand")
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "Romanian Daily",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Сменить тему",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("streak_badge")
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Text(
                        text = "$streak",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onStartDaily: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            val isHome = currentRoute == "home"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate("home") }
                    .padding(8.dp)
                    .testTag("nav_home")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Главная",
                    tint = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Главная",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Central Lesson FAB
            IconButton(
                onClick = onStartDaily,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .testTag("nav_start_daily")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Начать урок",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Practice
            val isPractice = currentRoute == "practice"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate("practice") }
                    .padding(8.dp)
                    .testTag("nav_practice")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Проверка",
                    tint = if (isPractice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Проверка",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPractice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dictionary
            val isDict = currentRoute == "dictionary"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate("dictionary") }
                    .padding(8.dp)
                    .testTag("nav_dictionary")
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Слова",
                    tint = if (isDict) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Слова",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDict) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpeakerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (isGhost) Color.Transparent
                else MaterialTheme.colorScheme.primaryContainer
            )
            .testTag("speaker_button")
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Прослушать произношение",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TopBackButton(
    onBack: () -> Unit,
    title: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
            modifier = Modifier.testTag("back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Назад",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        if (title.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun LetterPad(
    currentText: String,
    bankLetters: List<String>,
    onLetterClick: (String) -> Unit,
    onSpaceClick: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diacritics = listOf("ă", "â", "î", "ș", "ț")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Display typed text
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentText.ifEmpty { "слово появится здесь" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp
                ),
                color = if (currentText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center
            )
        }

        // Letter bank tiles
        if (bankLetters.isNotEmpty()) {
            OptFlowRow(letters = bankLetters, onSelect = onLetterClick)
        }

        // Diacritics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            diacritics.forEach { ch ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(horizontal = 3.dp)
                        .clickable { onLetterClick(ch) },
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = ch,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Control buttons: Space bar and Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable { onSpaceClick() }
                    .testTag("letterpad_space_button"),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "␣  ПРОБЕЛ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .width(68.dp)
                    .height(44.dp)
                    .clickable { onBackspace() }
                    .testTag("letterpad_backspace_button"),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "⌫",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@Composable
private fun OptFlowRow(
    letters: List<String>,
    onSelect: (String) -> Unit
) {
    // Break into chunks of up to 7 letters for clean visual display
    val chunks = letters.chunked(7)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        chunks.forEach { rowLetters ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowLetters.forEach { ch ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onSelect(ch) },
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ch,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
