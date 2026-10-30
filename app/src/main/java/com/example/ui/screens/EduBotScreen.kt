package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Chapter
import com.example.data.models.ChatMessage
import com.example.data.models.EducationalIllustration
import com.example.data.models.Subject
import com.example.ui.components.WebIllustrationViewerDialog
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduBotScreen(
    messages: List<ChatMessage>,
    chatInputText: String,
    isBotThinking: Boolean,
    isOfflineMode: Boolean = false,
    subjects: List<Subject> = emptyList(),
    chapters: List<Chapter> = emptyList(),
    selectedSubjectId: String = "math",
    selectedChapterId: String = "math_ch1",
    selectedLevel: String = "Terminale",
    selectedSeries: String = "D",
    illustrations: List<EducationalIllustration> = emptyList(),
    onSelectSubject: (String) -> Unit = {},
    onSelectChapter: (String) -> Unit = {},
    onChatInputTextChange: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    onClearHistory: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showWebIllustrationDialog by remember { mutableStateOf(false) }
    var showChapterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val activeSubject = remember(subjects, selectedSubjectId) {
        subjects.find { it.id == selectedSubjectId } 
            ?: subjects.firstOrNull() 
            ?: Subject("math", "Mathématiques", "Calculate", 0xFF1976D2, "")
    }

    val subjectChapters = remember(chapters, selectedSubjectId) {
        chapters.filter { it.subjectId == selectedSubjectId }
    }

    val activeChapter = remember(subjectChapters, chapters, selectedChapterId) {
        subjectChapters.find { it.id == selectedChapterId }
            ?: subjectChapters.firstOrNull()
            ?: chapters.firstOrNull()
    }

    if (showWebIllustrationDialog) {
        WebIllustrationViewerDialog(
            illustrations = illustrations,
            initialSubjectId = selectedSubjectId,
            isOfflineMode = isOfflineMode,
            onDismiss = { showWebIllustrationDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("edubot_screen")
    ) {
        // Top Header
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Guide Pédagogique",
                                tint = OrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Guide & Coach d'Exercices",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isOfflineMode) MaterialTheme.colorScheme.error else GreenSuccess,
                                    shape = CircleShape
                                ) {
                                    Box(modifier = Modifier.size(8.dp))
                                }
                            }
                            Text(
                                text = "Méthode Socratique : Apprendre sans tricher 🇨🇮",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Bouton Schémas & Illustrations Web
                    FilledTonalButton(
                        onClick = { showWebIllustrationDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OrangePrimary.copy(alpha = 0.15f),
                            contentColor = OrangePrimary
                        ),
                        modifier = Modifier.testTag("btn_illustrations_web")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Schémas",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Schémas Web",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sélecteur de Matière (Maths, PC, SVT, Philo, Français, Hist-Géo, Anglais)
                Text(
                    text = "1. Choisis la matière de ton exercice :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(subjects) { subj ->
                        val isSelected = subj.id == selectedSubjectId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSubject(subj.id) },
                            label = {
                                Text(
                                    text = subj.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sélecteur de Chapitre / Thème
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clickable { showChapterMenu = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeChapter?.title ?: "Chapitre en cours",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 240.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Changer",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showChapterMenu,
                            onDismissRequest = { showChapterMenu = false }
                        ) {
                            if (subjectChapters.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Programme général", fontSize = 13.sp) },
                                    onClick = { showChapterMenu = false }
                                )
                            } else {
                                subjectChapters.forEach { ch ->
                                    DropdownMenuItem(
                                        text = { Text(ch.title, fontSize = 13.sp) },
                                        onClick = {
                                            onSelectChapter(ch.id)
                                            showChapterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bouton Réinitialiser la discussion
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Effacer session",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Offline Banner
        if (isOfflineMode) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Hors-ligne",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mode Hors-ligne (0% Data) : Le guide utilise les fiches et méthodes intégrées.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Pédagogie Socratique : Action Chips
        val actionChips = listOf(
            "🧩 Débloquer mon exercice (Étape 1)" to "J'ai un exercice en ${activeSubject.name} (${activeChapter?.title ?: ""}). Voici l'énoncé : [copie ton énoncé ici]. Comment analyser les données et débuter ?",
            "💡 Indice n°1 (Formule clé)" to "Donne-moi uniquement la formule ou la règle de cours nécessaire pour résoudre cet exercice en ${activeSubject.name}, sans donner la solution.",
            "⚠️ Pièges fréquents du BAC" to "Quels sont les pièges classiques ou erreurs fréquentes à éviter sur le thème ${activeChapter?.title ?: activeSubject.name} au BAC CI ?",
            "🎯 Vérifier ma démarche" to "Voici ma proposition de calcul ou mon raisonnement : [écris ton idée]. Peux-tu me dire si ma méthode est correcte sans me donner la réponse ?"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actionChips) { (label, prompt) ->
                SuggestionChip(
                    onClick = { onSendMessage(prompt) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Chat & Guidance Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(OrangePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (!isUser) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Guide Socratique",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Explication", msg.text)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Texte copié !", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copier",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = msg.text,
                                fontSize = 13.5.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            if (isBotThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 42.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = OrangePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Le guide analyse ton exercice selon le programme CI...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Input Field
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = onChatInputTextChange,
                        placeholder = {
                            Text(
                                text = "Colle ou décris ton exercice en ${activeSubject.name}...",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onSendMessage(null) },
                        enabled = chatInputText.isNotBlank() && !isBotThinking,
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (chatInputText.isNotBlank()) OrangePrimary else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Envoyer",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🛡️ Garantie Anti-Tricherie Univers Nova Store • Apprentissage actif pour réussir le BAC",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
