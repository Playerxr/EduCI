package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Chapter

@Composable
fun QuizPracticeScreen(
    currentChapter: Chapter?,
    quizQuestionIndex: Int,
    selectedOptionIndex: Int?,
    isQuizSubmitted: Boolean,
    quizScore: Int,
    onSelectQuizOption: (Int) -> Unit,
    onSubmitQuizAnswer: () -> Unit,
    onNextQuizQuestion: () -> Unit,
    onResetQuiz: () -> Unit
) {
    var showRewardedAd by remember { mutableStateOf(false) }
    var rewardType by remember { mutableStateOf("") } // "HINT" or "DOUBLE_XP"
    var isXpDoubled by remember { mutableStateOf(false) }
    var isHintUnlocked by remember { mutableStateOf(false) }

    // Reset hint state automatically on next question
    LaunchedEffect(quizQuestionIndex) {
        isHintUnlocked = false
    }

    // Reset XP double state if starting a new quiz
    LaunchedEffect(currentChapter) {
        isXpDoubled = false
    }

    if (showRewardedAd) {
        com.example.ui.components.RewardedAdDialog(
            onRewardGranted = {
                if (rewardType == "HINT") {
                    isHintUnlocked = true
                } else if (rewardType == "DOUBLE_XP") {
                    isXpDoubled = true
                }
            },
            onDismiss = { showRewardedAd = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 40.dp, start = 20.dp, end = 20.dp)
            .testTag("quiz_practice_screen")
    ) {
        Text(
            text = "Entraînement Quiz BAC",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Teste tes connaissances sous conditions d'examen",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (currentChapter == null || currentChapter.quizQuestions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aucun quiz actif", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Sélectionne un chapitre dans l'onglet Cours pour commencer un quiz de révision.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val questions = currentChapter.quizQuestions
            if (quizQuestionIndex >= questions.size) {
                // Quiz Summary Result Screen
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophée",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quiz Terminé !",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ton score : $quizScore / ${questions.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isXpDoubled) "+${quizScore * 20} XP Gagnés " else "+${quizScore * 10} XP Gagnés ",
                                fontSize = 14.sp,
                                color = if (isXpDoubled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (isXpDoubled) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🏆 DOUBLE XP ACTIVÉ !",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Badge & Attestation de Leçon Débloqués !", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                                    Text("Reconnaissance interne d'accomplissement (Profil > Certificats)", fontSize = 11.sp, color = Color(0xFFF57F17))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (!isXpDoubled && quizScore > 0) {
                            Button(
                                onClick = {
                                    rewardType = "DOUBLE_XP"
                                    showRewardedAd = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Doubler mes XP ! (Spot Sponsorisé 📺)", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                isXpDoubled = false // Reset XP double on restart
                                onResetQuiz()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Recommencer le Quiz")
                        }

                    }
                }
            } else {
                val q = questions[quizQuestionIndex]
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Question ${quizQuestionIndex + 1} / ${questions.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Score : $quizScore",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = q.question,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        q.options.forEachIndexed { optIndex, optText ->
                            val isSelected = selectedOptionIndex == optIndex
                            val isCorrect = optIndex == q.correctAnswerIndex

                            val containerBg = when {
                                isQuizSubmitted && isCorrect -> Color(0xFFE8F5E9)
                                isQuizSubmitted && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Card(
                                onClick = { onSelectQuizOption(optIndex) },
                                colors = CardDefaults.cardColors(containerColor = containerBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${('A' + optIndex)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
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
                                    rewardType = "HINT"
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

                        Spacer(modifier = Modifier.height(20.dp))
                        if (isQuizSubmitted) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "💡 Explication :",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = q.explanation,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNextQuizQuestion,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Question Suivante")
                            }
                        } else {
                            Button(
                                onClick = onSubmitQuizAnswer,
                                enabled = selectedOptionIndex != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Valider ma Réponse")
                            }
                        }
                    }
                }
            }
        }
    }
}
