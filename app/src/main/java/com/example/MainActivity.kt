package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.viewmodel.EduViewModel
import com.example.ui.components.EduBottomBar
import com.example.ui.components.NetworkStatusBanner
import com.example.ui.components.BannerAdWidget
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.screens.*
import com.example.ui.theme.EduCITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            val viewModel: EduViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            EduCITheme(darkTheme = isDarkTheme) {
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
                val selectedSeries by viewModel.selectedSeries.collectAsStateWithLifecycle()
                val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

                val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
                val showInterstitialAd by viewModel.showInterstitialAd.collectAsStateWithLifecycle()
                val adCountdownSeconds by viewModel.adCountdownSeconds.collectAsStateWithLifecycle()

                val filteredChapters by viewModel.filteredChapters.collectAsStateWithLifecycle()
                val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
                val chapterDetailTab by viewModel.chapterDetailTab.collectAsStateWithLifecycle()
                val isBookmarked by viewModel.isCurrentChapterBookmarked.collectAsStateWithLifecycle()

                val quizQuestionIndex by viewModel.quizQuestionIndex.collectAsStateWithLifecycle()
                val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsStateWithLifecycle()
                val isQuizSubmitted by viewModel.isQuizSubmitted.collectAsStateWithLifecycle()
                val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()

                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val chatInputText by viewModel.chatInputText.collectAsStateWithLifecycle()
                val isBotThinking by viewModel.isBotThinking.collectAsStateWithLifecycle()

                val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
                val selectedSubjects by viewModel.selectedSubjects.collectAsStateWithLifecycle()
                val allHistorique by viewModel.allHistorique.collectAsStateWithLifecycle()

                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
                val notes by viewModel.notes.collectAsStateWithLifecycle()
                val certificates by viewModel.certificates.collectAsStateWithLifecycle()

                val totalXp = userProfile?.totalXp ?: 0
                val streakDays = userProfile?.streakDays ?: 0
                val coins = userProfile?.coins ?: 100

                if (userProfile == null || !userProfile!!.isOnboardingCompleted) {
                    OnboardingScreen(
                        onCompleteOnboarding = { name, level, series ->
                            viewModel.completeOnboarding(name, level, series)
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            Column {
                                if (!isOfflineMode) {
                                    BannerAdWidget(
                                        onTriggerAdNow = { viewModel.triggerAdImmediately() }
                                    )
                                }
                                EduBottomBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { viewModel.selectTab(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NetworkStatusBanner(
                                isOfflineMode = isOfflineMode,
                                countdownSeconds = adCountdownSeconds,
                                onToggleOffline = { viewModel.setOfflineMode(it) },
                                onTriggerAdNow = { viewModel.triggerAdImmediately() }
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    },
                                    label = "tab_transition"
                                ) { targetTab ->
                            when (targetTab) {
                                0 -> DashboardScreen(
                                    subjects = if (selectedSubjects.isNotEmpty()) selectedSubjects else allSubjects,
                                    selectedLevel = selectedLevel,
                                    selectedSeries = selectedSeries,
                                    selectedSubjectId = selectedSubjectId,
                                    searchQuery = searchQuery,
                                    totalXp = totalXp,
                                    streakDays = streakDays,
                                    coins = coins,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                    onSelectLevel = { viewModel.selectLevel(it) },
                                    onSelectSeries = { viewModel.selectSeries(it) },
                                    onSelectSubject = { viewModel.selectSubject(it) },
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onNavigateToSubjectChapters = { subjId ->
                                        viewModel.selectSubject(subjId)
                                        viewModel.selectTab(1)
                                    },
                                    onNavigateToQuizTab = { viewModel.selectTab(2) },
                                    onNavigateToEduBotTab = { viewModel.selectTab(3) }
                                )

                                1 -> LessonsScreen(
                                    subjects = if (selectedSubjects.isNotEmpty()) selectedSubjects else allSubjects,
                                    chapters = filteredChapters,
                                    currentChapter = currentChapter,
                                    selectedSubjectId = selectedSubjectId,
                                    selectedChapterDetailTab = chapterDetailTab,
                                    isBookmarked = isBookmarked,
                                    quizQuestionIndex = quizQuestionIndex,
                                    selectedOptionIndex = selectedOptionIndex,
                                    isQuizSubmitted = isQuizSubmitted,
                                    quizScore = quizScore,
                                    isOfflineMode = isOfflineMode,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                    onSelectSubject = { viewModel.selectSubject(it) },
                                    onSelectChapter = { viewModel.selectChapter(it) },
                                    onSelectChapterTab = { viewModel.selectChapterDetailTab(it) },
                                    onToggleBookmark = { viewModel.toggleBookmarkCurrentChapter() },
                                    onAddNote = { title, content -> viewModel.addPersonalNote(title, content) },
                                    onSelectQuizOption = { viewModel.selectQuizOption(it) },
                                    onSubmitQuizAnswer = { viewModel.submitQuizAnswer() },
                                    onNextQuizQuestion = { viewModel.nextQuizQuestion() }
                                )

                                2 -> QuizPracticeScreen(
                                    currentChapter = currentChapter,
                                    quizQuestionIndex = quizQuestionIndex,
                                    selectedOptionIndex = selectedOptionIndex,
                                    isQuizSubmitted = isQuizSubmitted,
                                    quizScore = quizScore,
                                    onSelectQuizOption = { viewModel.selectQuizOption(it) },
                                    onSubmitQuizAnswer = { viewModel.submitQuizAnswer() },
                                    onNextQuizQuestion = { viewModel.nextQuizQuestion() },
                                    onResetQuiz = { viewModel.resetQuizState() }
                                )

                                 3 -> EduBotScreen(
                                    messages = chatMessages,
                                    chatInputText = chatInputText,
                                    isBotThinking = isBotThinking,
                                    isOfflineMode = isOfflineMode,
                                    onChatInputTextChange = { viewModel.updateChatInput(it) },
                                    onSendMessage = { customPrompt -> viewModel.sendChatMessage(customPrompt) }
                                )

                                4 -> ProfileNotesScreen(
                                    userProfile = userProfile,
                                    selectedLevel = selectedLevel,
                                    selectedSeries = selectedSeries,
                                    totalXp = totalXp,
                                    streakDays = streakDays,
                                    coins = coins,
                                    bookmarks = bookmarks,
                                    notes = notes,
                                    certificates = certificates,
                                    allHistorique = allHistorique,
                                    allSubjects = allSubjects,
                                    isOfflineMode = isOfflineMode,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                    onToggleSubjectSelection = { subjId, isSelected ->
                                        viewModel.toggleSubjectSelection(subjId, isSelected)
                                    },
                                    onSaveProfile = { name, matricule, school, level, series, target ->
                                        viewModel.updateUserProfile(name, matricule, school, level, series, target)
                                    },
                                    onSelectLevel = { viewModel.selectLevel(it) },
                                    onSelectSeries = { viewModel.selectSeries(it) },
                                    onSelectChapter = { chId ->
                                        viewModel.selectChapter(chId)
                                        viewModel.selectTab(1)
                                    },
                                    onDeleteNote = { noteId -> viewModel.deletePersonalNote(noteId) },
                                    onRegisterWithAuth = { email, pass, name, onRes ->
                                        viewModel.registerStudent(email, pass, name, onRes)
                                    },
                                    onSignInWithAuth = { email, pass, onRes ->
                                        viewModel.signInStudent(email, pass, onRes)
                                    }
                                )

                            }
                        }
                    }
                }
                }
                }
                if (showInterstitialAd && !isOfflineMode) {
                    InterstitialAdDialog(
                        onDismiss = { viewModel.dismissAd() }
                    )
                }
            }
        }
    }
}
