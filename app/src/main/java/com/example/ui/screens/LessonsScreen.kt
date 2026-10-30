package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Chapter
import com.example.data.models.Subject
import com.example.ui.components.SubjectChipCard

@Composable
fun LessonsScreen(
    subjects: List<Subject>,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    selectedSubjectId: String,
    selectedChapterDetailTab: Int,
    isBookmarked: Boolean,
    quizQuestionIndex: Int,
    selectedOptionIndex: Int?,
    isQuizSubmitted: Boolean,
    quizScore: Int,
    isOfflineMode: Boolean = false,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onSelectSubject: (String) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectChapterTab: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onAddNote: (String, String) -> Unit,
    onSelectQuizOption: (Int) -> Unit,
    onSubmitQuizAnswer: () -> Unit,
    onNextQuizQuestion: () -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    var showRewardedAd by remember { mutableStateOf(false) }
    var isHintUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(quizQuestionIndex) {
        isHintUnlocked = false
    }

    if (showRewardedAd) {
        com.example.ui.components.RewardedAdDialog(
            onRewardGranted = { isHintUnlocked = true },
            onDismiss = { showRewardedAd = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("lessons_screen")
    ) {
        // Top Subject Switcher Header
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cours & Chapitres (1 à 8+)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Programme Scolaire National - Toutes Séries",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier.testTag("toggle_dark_theme_lessons")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Mode Nuit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow {
                    items(subjects) { subject ->
                        SubjectChipCard(
                            subject = subject,
                            isSelected = subject.id == selectedSubjectId,
                            onClick = { onSelectSubject(subject.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                
                // Banner for official MENA CI Portal
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOfflineMode) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Language,
                                contentDescription = "Ressources Web",
                                tint = if (isOfflineMode) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Portail e-School Côte d'Ivoire",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineMode) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isOfflineMode) "🔌 Indisponible Hors-ligne (Internet Requis)" else "Continuité pédagogique & cours de soutien",
                                    fontSize = 10.sp,
                                    color = if (isOfflineMode) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        if (!isOfflineMode) {
                            Row {
                                SuggestionChip(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-school.eduweb.ci/course/index.php?categoryid=107"))
                                        context.startActivity(intent)
                                    },
                                    label = { Text("Continuité", fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                SuggestionChip(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-school.eduweb.ci/course/index.php?categoryid=5"))
                                        context.startActivity(intent)
                                    },
                                    label = { Text("Cours 2nd Cycle", fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chapter List Selector Bar
        if (chapters.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = chapters.indexOfFirst { it.id == currentChapter?.id }.coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                chapters.forEachIndexed { index, chapter ->
                    Tab(
                        selected = currentChapter?.id == chapter.id,
                        onClick = { onSelectChapter(chapter.id) },
                        text = {
                            Text(
                                text = chapter.title,
                                fontWeight = if (currentChapter?.id == chapter.id) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }

        if (currentChapter == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun chapitre disponible pour ce filtre.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Main Content Area
            Column(modifier = Modifier.weight(1f)) {
                // Chapter Header Bar with Action Icons
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentChapter.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentChapter.level} - Série ${currentChapter.series}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row {
                            IconButton(onClick = onToggleBookmark) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Favori",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showNoteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.NoteAdd,
                                    contentDescription = "Ajouter Note",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Sub-tabs: Résumé, Cours, Formules, Quiz
                TabRow(
                    selectedTabIndex = selectedChapterDetailTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val subTabs = listOf("Résumé", "Cours complet", "Formules & Astuces", "Quiz")
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedChapterDetailTab == index,
                            onClick = { onSelectChapterTab(index) },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                // Content view based on sub-tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (selectedChapterDetailTab) {
                        0 -> { // Résumé
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Aperçu Synthétique",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentChapter.summary,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Points Clés à Retenir",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        currentChapter.keyPoints.forEach { point ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = point,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> { // Cours complet
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = currentChapter.fullContent,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        2 -> { // Formules & Astuces BAC
                            item {
                                if (currentChapter.formulaSheet != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Functions, contentDescription = null, tint = Color(0xFFE65100))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Formules Clés",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = currentChapter.formulaSheet,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF3E2723)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF57F17))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Conseils des Correcteurs du BAC",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        currentChapter.bacExamTips.forEach { tip ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(tip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> { // Quiz
                            item {
                                val questions = currentChapter.quizQuestions
                                if (questions.isEmpty()) {
                                    Text("Aucune question de quiz pour ce chapitre.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else if (quizQuestionIndex < questions.size) {
                                    val q = questions[quizQuestionIndex]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Question ${quizQuestionIndex + 1}/${questions.size}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Score: $quizScore",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(q.question, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                            Spacer(modifier = Modifier.height(16.dp))
                                            q.options.forEachIndexed { optIndex, optText ->
                                                val isSelected = selectedOptionIndex == optIndex
                                                val isCorrect = optIndex == q.correctAnswerIndex

                                                val btnBg = when {
                                                    isQuizSubmitted && isCorrect -> Color(0xFFE8F5E9)
                                                    isQuizSubmitted && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }

                                                Card(
                                                    onClick = { onSelectQuizOption(optIndex) },
                                                    colors = CardDefaults.cardColors(containerColor = btnBg),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${('A' + optIndex)}. ",
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(optText, fontSize = 14.sp)
                                                    }
                                                }
                                            }

                                            if (isHintUnlocked) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF57F17))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Indice débloqué : Élimine les fausses pistes ! Le sponsor te confie que la bonne réponse est l'option ${('A' + q.correctAnswerIndex)}.",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF5D4037)
                                                        )
                                                    }
                                                }
                                            }

                                            if (!isQuizSubmitted && !isHintUnlocked) {
                                                OutlinedButton(
                                                    onClick = {
                                                        showRewardedAd = true
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.secondary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Obtenir un indice (Spot Vidéo 🎁)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            if (isQuizSubmitted) {
                                                Text(
                                                    text = "Explication : ${q.explanation}",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = onNextQuizQuestion,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Question Suivante")
                                                }
                                            } else {
                                                Button(
                                                    onClick = onSubmitQuizAnswer,
                                                    enabled = selectedOptionIndex != null,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Valider la Réponse")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Personal Note Dialog
        if (showNoteDialog) {
            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = { Text("Ajouter une note personnelle") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = noteTitleInput,
                            onValueChange = { noteTitleInput = it },
                            label = { Text("Titre de la note") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteContentInput,
                            onValueChange = { noteContentInput = it },
                            label = { Text("Contenu de ta note") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAddNote(noteTitleInput, noteContentInput)
                            noteTitleInput = ""
                            noteContentInput = ""
                            showNoteDialog = false
                        }
                    ) {
                        Text("Enregistrer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}
