package com.example.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.local.EduDatabase
import com.example.data.models.*
import com.example.data.repository.EduRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EduRepository()
    private val db = EduDatabase.getDatabase(application)
    private val bookmarkDao = db.bookmarkDao()
    private val noteDao = db.noteDao()
    private val userProfileDao = db.userProfileDao()
    private val certificateDao = db.certificateDao()
    private val completedChapterDao = db.completedChapterDao()
    private val subjectDao = db.subjectDao()
    private val chapterDao = db.chapterDao()
    private val historiqueDao = db.historiqueDao()

    // Room Historique State Flow
    val allHistorique: StateFlow<List<HistoriqueEntity>> = historiqueDao.getAllHistorique()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Subject State Flows
    val allSubjects: StateFlow<List<Subject>> = subjectDao.getAllSubjects()
        .map { list -> list.map { it.toSubject() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getDefaultSecondCycleSubjects())

    val selectedSubjects: StateFlow<List<Subject>> = subjectDao.getSelectedSubjects()
        .map { list -> list.map { it.toSubject() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getDefaultSecondCycleSubjects().filter { it.isSelected })

    // Room Chapters State Flow
    val allChapters: StateFlow<List<Chapter>> = chapterDao.getAllChapters()
        .map { list -> list.map { it.toChapter() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getSampleChapters())

    // Legacy fallback subject list
    val subjects = repository.getSubjects()

    fun toggleSubjectSelection(subjectId: String, isSelected: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            subjectDao.updateSubjectSelection(subjectId, isSelected)
        }
    }

    val selectedTab = MutableStateFlow(0) // 0: Home/Dashboard, 1: Lessons, 2: Quiz, 3: EduBot, 4: Profile/Notes
    val selectedLevel = MutableStateFlow("Terminale")
    val selectedSeries = MutableStateFlow("D")
    val selectedSubjectId = MutableStateFlow("math")
    val searchQuery = MutableStateFlow("")
    val isDarkTheme = MutableStateFlow(false)

    fun toggleDarkTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    val filteredChapters: StateFlow<List<Chapter>> = combine(
        allChapters, selectedSubjectId, selectedLevel, selectedSeries, searchQuery
    ) { chapterList, subjId, lvl, srs, query ->
        val activeList = if (chapterList.isNotEmpty()) chapterList else repository.getSampleChapters()
        activeList
            .filter { ch -> ch.subjectId == subjId }
            .map { ch -> adaptChapterForLevelAndSeries(ch, lvl, srs) }
            .filter { ch ->
                query.isEmpty() || 
                ch.title.contains(query, ignoreCase = true) || 
                ch.summary.contains(query, ignoreCase = true)
            }
            .sortedBy { it.chapterNumber }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedChapterId = MutableStateFlow("math_ch1")

    val currentChapter: StateFlow<Chapter?> = combine(selectedChapterId, filteredChapters, allChapters) { chId, filteredList, fullList ->
        filteredList.find { it.id == chId }
            ?: filteredList.firstOrNull()
            ?: fullList.find { it.id == chId }
            ?: fullList.firstOrNull()
            ?: repository.getSampleChapters().firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getSampleChapters().firstOrNull())

    val chapterDetailTab = MutableStateFlow(0) // 0: Résumé, 1: Cours Complet, 2: Formules/Astuces, 3: Quiz

    val isCurrentChapterBookmarked: StateFlow<Boolean> = currentChapter.flatMapLatest { ch ->
        if (ch != null) bookmarkDao.isBookmarked(ch.id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCurrentChapterCertified: StateFlow<Boolean> = currentChapter.flatMapLatest { ch ->
        if (ch != null) certificateDao.isChapterCertified(ch.id) else flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Quiz State
    val quizQuestionIndex = MutableStateFlow(0)
    val selectedOptionIndex = MutableStateFlow<Int?>(null)
    val isQuizSubmitted = MutableStateFlow(false)
    val quizScore = MutableStateFlow(0)

    // Educational Illustrations & Web Resources
    val educationalIllustrations = MutableStateFlow(repository.getEducationalIllustrations())

    // Guidance Context: Selected Subject and Chapter for Socratic AI
    val guidanceSubjectId = MutableStateFlow("math")
    val guidanceChapterId = MutableStateFlow("math_ch1")

    fun setGuidanceSubject(subjectId: String) {
        guidanceSubjectId.value = subjectId
        selectedSubjectId.value = subjectId
        val chList = allChapters.value.filter { it.subjectId == subjectId }
        if (chList.isNotEmpty()) {
            guidanceChapterId.value = chList.first().id
            selectedChapterId.value = chList.first().id
        }
    }

    fun setGuidanceChapter(chapterId: String) {
        guidanceChapterId.value = chapterId
        selectedChapterId.value = chapterId
    }

    // EduBot / Socratic Guidance Chat State
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "1",
                sender = "bot",
                text = "Bienvenue dans ton Guide Pédagogique d'Exercices 🇨🇮 !\n\n🎯 **Méthode Socratique (Apprentissage Actif)** : Je ne résous pas les devoirs à ta place. Mon rôle est de te guider pas à pas, de t'expliquer les formules et de te donner des indices pour que tu réussisses par toi-même au BAC !\n\nChoisis ta matière en haut (Maths, PC, SVT...) et écris ton exercice ou ce qui te bloque."
            )
        )
    )
    val chatInputText = MutableStateFlow("")
    val isBotThinking = MutableStateFlow(false)

    fun clearChatHistory() {
        val subjObj = allSubjects.value.find { it.id == guidanceSubjectId.value }
        val subjName = subjObj?.name ?: "cette matière"
        chatMessages.value = listOf(
            ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "bot",
                text = "Session réinitialisée pour **$subjName** 🎯. Pose ton énoncé ou choisis une action guidée (Débloquer un exercice, Indice progressif, Schémas du Net)."
            )
        )
    }

    // Room persistence flows
    val bookmarks = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile = userProfileDao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    val certificates = certificateDao.getAllCertificates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedChapters = completedChapterDao.getAllCompletedChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // OkHttpClient for Gemini API call
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    // Ads and Offline mode state
    val isOfflineMode = MutableStateFlow(false)
    val showInterstitialAd = MutableStateFlow(false)
    val adCountdownSeconds = MutableStateFlow(600) // 10 minutes (600 seconds)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (subjectDao.getSubjectCount() == 0) {
                val initialSubjects = repository.getDefaultSecondCycleSubjects().map { it.toEntity() }
                subjectDao.insertSubjects(initialSubjects)
            }
            if (chapterDao.getChapterCount() == 0) {
                val initialChapters = repository.getSampleChapters().map { it.toEntity() }
                chapterDao.insertChapters(initialChapters)
            }
            if (historiqueDao.getHistoriqueCount() == 0) {
                val initialHistorique = repository.getDefaultHistorique()
                historiqueDao.insertAllHistorique(initialHistorique)
            }
        }
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null && (profile.name.isNotEmpty() || profile.isOnboardingCompleted)) {
                    selectedLevel.value = profile.selectedLevel
                    selectedSeries.value = profile.selectedSeries
                }
            }
        }
        // Background timer for advertisement displaying every 10 minutes when online
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (!isOfflineMode.value) {
                    if (adCountdownSeconds.value > 0) {
                        adCountdownSeconds.value -= 1
                    } else {
                        showInterstitialAd.value = true
                        adCountdownSeconds.value = 600 // Reset to 10 minutes
                    }
                }
            }
        }
    }


    fun selectTab(index: Int) {
        selectedTab.value = index
    }

    fun setOfflineMode(offline: Boolean) {
        isOfflineMode.value = offline
        if (offline) {
            showInterstitialAd.value = false
        }
    }

    fun dismissAd() {
        showInterstitialAd.value = false
    }

    fun triggerAdImmediately() {
        if (!isOfflineMode.value) {
            showInterstitialAd.value = true
            adCountdownSeconds.value = 600
        }
    }

    fun selectLevel(level: String) {
        selectedLevel.value = level
    }

    fun selectSeries(series: String) {
        selectedSeries.value = series
    }

    fun selectSubject(subjectId: String) {
        selectedSubjectId.value = subjectId
        val firstCh = allChapters.value.find { it.subjectId == subjectId } ?: repository.getSampleChapters().find { it.subjectId == subjectId }
        if (firstCh != null) {
            selectedChapterId.value = firstCh.id
        }
    }

    fun selectChapter(chapterId: String) {
        selectedChapterId.value = chapterId
        resetQuizState()
    }

    fun selectChapterDetailTab(tabIndex: Int) {
        chapterDetailTab.value = tabIndex
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleBookmarkCurrentChapter() {
        val ch = currentChapter.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (isCurrentChapterBookmarked.value) {
                bookmarkDao.deleteBookmarkByChapterId(ch.id)
            } else {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        chapterId = ch.id,
                        subjectId = ch.subjectId,
                        chapterTitle = ch.title
                    )
                )
            }
        }
    }

    fun addPersonalNote(title: String, content: String) {
        val ch = currentChapter.value ?: return
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertNote(
                NoteEntity(
                    id = System.currentTimeMillis().toString(),
                    chapterId = ch.id,
                    chapterTitle = ch.title,
                    noteTitle = title,
                    noteContent = content
                )
            )
        }
    }

    fun deletePersonalNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNote(noteId)
        }
    }

    fun selectQuizOption(index: Int) {
        if (!isQuizSubmitted.value) {
            selectedOptionIndex.value = index
        }
    }

    fun submitQuizAnswer() {
        val ch = currentChapter.value ?: return
        val questions = ch.quizQuestions
        val currentIndex = quizQuestionIndex.value
        if (currentIndex < questions.size) {
            val q = questions[currentIndex]
            val selected = selectedOptionIndex.value
            if (selected != null && !isQuizSubmitted.value) {
                isQuizSubmitted.value = true
                if (selected == q.correctAnswerIndex) {
                    quizScore.value += 1
                    updateUserXp(10)
                    earnCoins(5)
                }
            }
        }
    }

    fun nextQuizQuestion() {
        val ch = currentChapter.value ?: return
        val questions = ch.quizQuestions
        if (quizQuestionIndex.value + 1 < questions.size) {
            quizQuestionIndex.value += 1
            selectedOptionIndex.value = null
            isQuizSubmitted.value = false
        } else {
            // Quiz finished - record completion and award bonus XP
            claimCertificateForCurrentChapter()
        }
    }

    fun claimCertificateForCurrentChapter() {
        val ch = currentChapter.value ?: return
        val questions = ch.quizQuestions
        if (questions.isEmpty()) return

        val score = quizScore.value
        val percentage = ((score.toFloat() / questions.size.toFloat()) * 100).toInt()
        val profile = userProfile.value
        val studentName = if (!profile?.name.isNullOrBlank()) profile!!.name else if (!profile?.matricule.isNullOrBlank()) "Élève (${profile!!.matricule})" else "Élève Non Inscrit"

        val subjectObj = subjects.find { it.id == ch.subjectId }
        val subjectName = subjectObj?.name ?: ch.subjectId

        val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date())
        val randomCode = (1000..9999).random()
        val certCode = "EDU-CI-2026-${ch.subjectId.uppercase()}-$randomCode"

        viewModelScope.launch(Dispatchers.IO) {
            // Mark chapter completed in Room DB
            completedChapterDao.markChapterCompleted(
                CompletedChapterEntity(
                    chapterId = ch.id,
                    subjectId = ch.subjectId,
                    quizHighScore = score
                )
            )

            // Insert into revision history (Historique)
            val feedbackText = when {
                percentage == 100 -> "Parfait ! Maîtrise absolue du chapitre."
                percentage >= 80 -> "Excellent travail ! De très bonnes connaissances."
                percentage >= 60 -> "Bien joué ! Quelques points restent à consolider."
                else -> "Continuez à réviser, vous allez progresser !"
            }
            historiqueDao.insertHistorique(
                HistoriqueEntity(
                    subjectId = ch.subjectId,
                    subjectName = subjectName,
                    chapterId = ch.id,
                    chapterTitle = ch.title,
                    score = score,
                    totalQuestions = questions.size,
                    scorePercentage = percentage,
                    revisionDate = System.currentTimeMillis(),
                    revisionType = "QUIZ",
                    durationSeconds = (60..180).random(),
                    feedback = feedbackText
                )
            )

            // Insert official Certificate Entity in Room DB
            certificateDao.insertCertificate(
                CertificateEntity(
                    id = "cert_${ch.id}",
                    subjectId = ch.subjectId,
                    subjectName = subjectName,
                    chapterId = ch.id,
                    chapterTitle = ch.title,
                    level = ch.level,
                    series = ch.series,
                    studentName = studentName,
                    scorePercentage = percentage,
                    issueDate = dateFormatted,
                    certificateCode = certCode
                )
            )

            // Award extra XP & Coins
            updateUserXp(25)
            earnCoins(20)
        }
    }

    fun resetQuizState() {
        quizQuestionIndex.value = 0
        selectedOptionIndex.value = null
        isQuizSubmitted.value = false
        quizScore.value = 0
    }

    fun completeOnboarding(name: String, level: String, series: String) {
        selectedLevel.value = level
        selectedSeries.value = series
        val finalMatricule = "MAT-2026-CI-" + (1000..9999).random()
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value ?: UserProfileEntity()
            userProfileDao.insertOrUpdateProfile(
                current.copy(
                    name = name.trim(),
                    selectedLevel = level,
                    selectedSeries = series,
                    matricule = finalMatricule,
                    isOnboardingCompleted = true
                )
            )
        }
    }

    fun updateUserProfile(name: String, matricule: String, schoolName: String, level: String, series: String, examTarget: String) {
        selectedLevel.value = level
        selectedSeries.value = series
        val finalMatricule = if (matricule.isBlank()) "MAT-2026-CI-" + (1000..9999).random() else matricule.trim().uppercase()
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value ?: UserProfileEntity()
            userProfileDao.insertOrUpdateProfile(
                current.copy(
                    name = name.trim(),
                    matricule = finalMatricule,
                    schoolName = schoolName.trim(),
                    selectedLevel = level,
                    selectedSeries = series,
                    examTarget = examTarget.trim()
                )
            )
        }
    }

    private fun updateUserXp(addXp: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value ?: UserProfileEntity()
            userProfileDao.insertOrUpdateProfile(
                current.copy(totalXp = current.totalXp + addXp)
            )
        }
    }

    fun earnCoins(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value ?: UserProfileEntity()
            userProfileDao.insertOrUpdateProfile(
                current.copy(coins = current.coins + amount)
            )
        }
    }

    fun spendCoins(amount: Int): Boolean {
        val current = userProfile.value ?: UserProfileEntity()
        if (current.coins >= amount) {
            viewModelScope.launch(Dispatchers.IO) {
                userProfileDao.insertOrUpdateProfile(
                    current.copy(coins = current.coins - amount)
                )
            }
            return true
        }
        return false
    }

    fun updateChatInput(text: String) {
        chatInputText.value = text
    }

    fun sendChatMessage(customPrompt: String? = null) {
        val prompt = customPrompt ?: chatInputText.value.trim()
        if (prompt.isBlank()) return

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "user",
            text = prompt
        )

        chatMessages.value = chatMessages.value + userMsg
        if (customPrompt == null) chatInputText.value = ""
        isBotThinking.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val replyText = fetchGeminiOrFallbackReply(prompt)
            val botMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "bot",
                text = replyText
            )
            chatMessages.value = chatMessages.value + botMsg
            isBotThinking.value = false
        }
    }

    private fun fetchGeminiOrFallbackReply(userPrompt: String): String {
        val subjObj = allSubjects.value.find { it.id == guidanceSubjectId.value } 
            ?: allSubjects.value.find { it.id == selectedSubjectId.value }
            ?: repository.getDefaultSecondCycleSubjects().first()
        val subjectName = subjObj.name

        val activeCh = allChapters.value.find { it.id == guidanceChapterId.value } 
            ?: currentChapter.value
        val chapterTitle = activeCh?.title ?: "Programme Officiel Second Cycle"

        val level = selectedLevel.value
        val series = selectedSeries.value

        // Quick anti-cheating check for direct cheating requests
        val promptLower = userPrompt.lowercase()
        val isCheatingAttempt = promptLower.contains("triche") || 
                promptLower.contains("donne moi la reponse de mon devoir") ||
                promptLower.contains("fais mon devoir a ma place") ||
                promptLower.contains("resolvant mon examen en direct") ||
                promptLower.contains("fais l'exercice a ma place")

        if (isCheatingAttempt) {
            return "🛡️ **Principe Pédagogique du Guide EduCI** :\n\nEn tant que tuteur socratique conforme au Ministère de l'Éducation Nationale (MENA) de Côte d'Ivoire, je ne fais jamais les exercices à votre place.\n\nEn revanche, voici comment nous allons procéder ensemble :\n1. Donne-moi les données de ton énoncé.\n2. Je t'indique la formule ou le théorème clé de **$subjectName**.\n3. Tu calcules la première étape et je te dis immédiatement si ta démarche est exacte !"
        }

        if (isOfflineMode.value) {
            val offlineResponse = getOfflineLocalReply(userPrompt, subjectName, chapterTitle)
            return "🔌 **[Mode Hors-ligne Actif - 0% Data]** Guide méthodologique EduCI pour **$subjectName** :\n\n$offlineResponse"
        }

        try {
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val sysInstruction = """
                    Tu es le Guide Pédagogique et Tuteur Socratique Officiel d'EduCI pour les élèves de $level Série $series en Côte d'Ivoire (Programme National MENA) 🇨🇮.
                    
                    CONTEXTE SCOLAIRE ACTUEL :
                    - Matière sélectionnée : $subjectName
                    - Chapitre / Thème : $chapterTitle
                    
                    RÈGLES D'OR DU GUIDAGE PÉDAGOGIQUE (MÉTHODE SOCRATIQUE) :
                    1. NE JAMAIS RÉSOUDRE L'EXERCICE À LA PLACE DE L'ÉLÈVE ! L'élève doit comprendre et s'entraîner activement.
                    2. SI L'ÉLÈVE SOUMET UN ÉNONCÉ D'EXERCICE OU DE DEVOIR :
                       - Étape 1 : Identifie les données utiles et ce qu'il faut déterminer/prouver.
                       - Étape 2 : Rappelle la formule officielle, le théorème ou la règle de cours nécessaire.
                       - Étape 3 : Donne la première piste de réflexion ou la première équation à poser.
                       - Étape 4 : Pose une question claire à l'élève pour qu'il calcule ou raisonne sur cette première étape avant d'aller plus loin !
                    3. SI L'ÉLÈVE DEMANDE UN INDICE : Fournis un indice progressif (Indice n°1 = direction de raisonnement, Indice n°2 = formule détaillée, Indice n°3 = piège à éviter).
                    4. SI L'ÉLÈVE PROPOSE UN RÉSULTAT : Valide ce qui est correct, signale l'erreur éventuelle de calcul ou de signe avec bienveillance, sans donner la solution brute.
                    5. Sois chaleureux, structuré avec des puces Markdown, utilise les notations exactes du programme ivoirien.
                """.trimIndent()
                
                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$sysInstruction\n\nQuestion ou exercice de l'élève en $subjectName : $userPrompt"))
                            })
                        })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonResp = JSONObject(responseBody)
                        val candidates = jsonResp.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCand = candidates.getJSONObject(0)
                            val content = firstCand.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                return parts.getJSONObject(0).getString("text")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Offline fallback educational response if internet error
        return getOfflineLocalReply(userPrompt, subjectName, chapterTitle)
    }

    private fun getOfflineLocalReply(userPrompt: String, subjectName: String, chapterTitle: String): String {
        val p = userPrompt.lowercase()
        return when {
            subjectName.contains("math", ignoreCase = true) -> {
                when {
                    p.contains("limite") || p.contains("tvi") ->
                        "📌 **Méthode d'Analyse TVI & Limites (Maths $chapterTitle)** :\n\n1. **Forme Indéterminée ?** (0/0, ∞/∞, +∞-∞, 0×∞) : Factorise par le terme de plus haut degré ou utilise l'expression conjuguée.\n2. **TVI (Théorème des Valeurs Intermédiaires)** :\n   - Vérifie la continuité de f sur [a, b].\n   - Calcule f(a) et f(b) et montre que 0 est compris entre f(a) et f(b) (f(a)·f(b) < 0).\n   - Conclus : Il existe au moins un réel α tel que f(α) = 0.\n\n💡 *Quel calcul bloques-tu précisément ? Écris ton début d'expression !*"

                    p.contains("complexe") || p.contains("forme trigonométrique") ->
                        "📌 **Méthode Nombres Complexes** :\n\n- Module : |z| = r = √(a² + b²)\n- Argument θ : cos(θ) = a/r et sin(θ) = b/r\n- Forme trigonométrique : z = r(cos θ + i sin θ) = r e^(iθ)\n\n💡 *Dis-moi les valeurs de a et b de ton énoncé pour qu'on calcule r ensemble !*"

                    else ->
                        "📌 **Guidage Mathématiques - $chapterTitle** :\n\nPour avancer sans te bloquer :\n1. Écris clairement la fonction ou les équations données.\n2. Précise le domaine de définition (ex: x ≠ 0, x > 0 pour ln).\n3. Calcule la dérivée f'(x) si l'exercice porte sur les variations.\n\nQuelle est la première question de ton exercice ?"
                }
            }

            subjectName.contains("physique", ignoreCase = true) || subjectName.contains("chimie", ignoreCase = true) -> {
                when {
                    p.contains("newton") || p.contains("mouvement") || p.contains("projectile") ->
                        "⚡ **Guidage Mécanique Newtonienne (PC)** :\n\n1. Définis le système étudié (ex: le solide S) et le référentiel (terrestre supposé galiléen).\n2. Fais le bilan des forces appliquées (Poids P = mg, frottements f, réaction R).\n3. Applique la 2ème loi de Newton : Σ F_ext = m · a.\n4. Projette sur les axes (Ox, Oy) pour obtenir les équations différentielles.\n\n💡 *Quelles forces as-tu identifiées dans ton énoncé ?*"

                    p.contains("dosage") || p.contains("acide") || p.contains("ph") ->
                        "⚗️ **Guidage Chimie des Solutions** :\n\n- Équation bilan du dosage : Acide + Base ⟶ Sel + Eau.\n- À l'équivalence acido-basique : n_A (acide versé) = n_B (base introduite).\n- Relation fondamentale : C_A · V_A = C_B · V_E.\n\n💡 *Quelle grandeur recherches-tu (C_A ou V_E) ?*"

                    else ->
                        "⚡ **Guidage Physique-Chimie - $chapterTitle** :\n\n1. Note toutes les grandeurs données avec leurs unités SI (m, s, kg, V, A, mol/L).\n2. Repère la loi physique correspondante (ex: Newton, Ohm, décroissance radioactive, pH).\n3. Isole l'inconnue avant de faire l'application numérique.\n\nQuel est l'énoncé de ta question ?"
                }
            }

            subjectName.contains("svt", ignoreCase = true) -> {
                "🧬 **Guidage SVT - $chapterTitle** :\n\n- En SVT au BAC CI, applique la démarche scientifique : **Je vois que... (Constat) + Or je sais que... (Rappel du cours) + Donc j'en déduis que... (Conclusion)**.\n- Pour les schémas : pense au titre complet, aux flèches horizontales avec légende précise, et à l'échelle.\n- Ouvre l'outil **Illustrations du Net** pour visualiser le schéma officiel du chapitre !"
            }

            subjectName.contains("philo", ignoreCase = true) || subjectName.contains("français", ignoreCase = true) -> {
                "🏛️ **Méthodologie Épreuve Littéraire & Philo** :\n\n1. **Analyse du sujet** : Définis chaque terme clé et leurs relations logiques (opposition, cause, conséquence).\n2. **Problématique** : Reformule le problème central sous forme de tension philosophique (ex: \"Dans quelle mesure peut-on affirmer que...\").\n3. **Plan dialectique** : Thèse (affirmation) ⟶ Antithèse (limite/objection) ⟶ Synthèse (dépassement fondé)."
            }

            else ->
                "🇨🇮 **Guide Pédagogique EduCI ($subjectName - $chapterTitle)** :\n\nPour progresser efficacement dans cette matière :\n1. Revois les définitions indispensables du cours.\n2. Note les données clés de ton exercice.\n3. Identifie la question précise qui te bloque.\n\nPose-moi ta question spécifique pour que nous la débloquions ensemble étape par étape !"
        }
    }


    val authManager = AuthManager()

    fun registerStudent(email: String, pass: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        authManager.registerWithEmail(email, pass, displayName) { success, err ->
            if (success) {
                updateUserProfile(displayName, authManager.getStudentUniqueId(), "", selectedLevel.value, selectedSeries.value, "")
            }
            onResult(success, err)
        }
    }

    fun signInStudent(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        authManager.signInWithEmail(email, pass) { success, err ->
            if (success) {
                val name = authManager.getStudentDisplayName() ?: ""
                updateUserProfile(name, authManager.getStudentUniqueId(), "", selectedLevel.value, selectedSeries.value, "")
            }
            onResult(success, err)
        }
    }

    private fun adaptChapterForLevelAndSeries(ch: Chapter, lvl: String, srs: String): Chapter {
        if (lvl == "Terminale") {
            val newTitle = ch.title
                .replace("Série D/C", "Série $srs")
                .replace("Série D", "Série $srs")
                .replace("Série C", "Série $srs")
                .replace("Série A", "Série $srs")
                .replace("Terminale D/C", "Terminale $srs")
                .replace("Terminale D", "Terminale $srs")
                .replace("Terminale C", "Terminale $srs")
                .replace("Terminale A", "Terminale $srs")
            return ch.copy(
                title = newTitle,
                level = lvl,
                series = srs
            )
        }

        val chNum = ch.chapterNumber
        var title = ch.title
        var summary = ch.summary
        var keyPoints = ch.keyPoints
        var fullContent = ch.fullContent
        var quizQuestions = ch.quizQuestions

        when (ch.subjectId) {
            "math" -> {
                if (lvl == "Seconde") {
                    when (chNum) {
                        1 -> {
                            title = "Chapitre 1 : Ensembles de Nombres & Calculs"
                            summary = "Comprendre les différents ensembles de nombres (N, Z, D, Q, R) et les techniques de calcul de base."
                            keyPoints = listOf(
                                "📌 Ensembles de nombres : N ⊂ Z ⊂ D ⊂ Q ⊂ R",
                                "💡 Intervalles de R, intersections et unions",
                                "⚡ Valeur absolue : |x| est la distance de x à l'origine",
                                "📐 Puissances, fractions et écriture scientifique"
                            )
                        }
                        2 -> {
                            title = "Chapitre 2 : Généralités sur les Fonctions"
                            summary = "Introduction aux fonctions, domaine de définition, image, antécédent et courbe représentative."
                            keyPoints = listOf(
                                "📌 Domaine de définition D_f d'une fonction",
                                "💡 Détermination graphique des images et antécédents",
                                "⚡ Parité : fonctions paires (symétrie axe ordonnées) et impaires",
                                "📐 Variations d'une fonction (croissance, décroissance)"
                            )
                        }
                        3 -> {
                            title = "Chapitre 3 : Fonctions de Référence"
                            summary = "Étude détaillée des fonctions affines, carré, inverse et racine carrée."
                            keyPoints = listOf(
                                "📌 Fonction affine : f(x) = ax + b, droite de pente a",
                                "💡 Fonction Carré : f(x) = x², parabole symétrique",
                                "⚡ Fonction Inverse : f(x) = 1/x, hyperbole symétrique",
                                "📐 Comparaisons d'expressions et résolution graphique"
                            )
                        }
                        4 -> {
                            title = "Chapitre 4 : Équations & Inéquations du 1er Degré"
                            summary = "Méthodes algébriques et graphiques pour résoudre les équations et inéquations de premier degré."
                            keyPoints = listOf(
                                "📌 Résolution de ax + b = 0 et ax + b > 0",
                                "💡 Tableau de signes d'un binôme du premier degré",
                                "⚡ Résolution d'équations produits et quotients",
                                "📐 Modélisation de problèmes réels"
                            )
                        }
                        5 -> {
                            title = "Chapitre 5 : Vecteurs du Plan & Colinéarité"
                            summary = "Définition des vecteurs, égalité, relation de Chasles et colinéarité."
                            keyPoints = listOf(
                                "📌 Vecteurs : direction, sens et norme",
                                "💡 Relation de Chasles : AB + BC = AC",
                                "⚡ Colinéarité : u et v colinéaires s'il existe k tel que u = k.v",
                                "📐 Coordonnées d'un vecteur et déterminant"
                            )
                        }
                        6 -> {
                            title = "Chapitre 6 : Statistiques Descriptives"
                            summary = "Savoir organiser des données statistiques et calculer les indicateurs de position et de dispersion."
                            keyPoints = listOf(
                                "📌 Effectif, fréquence et effectifs cumulés",
                                "💡 Indicateurs de position : Moyenne, Médiane, Mode",
                                "⚡ Indicateurs de dispersion : Étendue, Quartiles (Q1, Q3)",
                                "📐 Représentation : diagramme en bâtons, histogramme"
                            )
                        }
                        7 -> {
                            title = "Chapitre 7 : Trigonométrie dans le Cercle"
                            summary = "Définir le cercle trigonométrique, l'enroulement et le cosinus/sinus."
                            keyPoints = listOf(
                                "📌 Cercle trigonométrique de rayon R = 1 orienté",
                                "💡 Mesure principale d'un angle en radians",
                                "⚡ Formules fondamentales : cos²(x) + sin²(x) = 1",
                                "📐 Valeurs remarquables : 0, π/6, π/4, π/3, π/2"
                            )
                        }
                        else -> {
                            title = "Chapitre 8 : Géométrie dans l'Espace (Solides)"
                            summary = "Positions relatives de droites et de plans dans l'espace. Calculs de volumes."
                            keyPoints = listOf(
                                "📌 Parallélisme et orthogonalité dans l'espace",
                                "💡 Représentation en perspective cavalière",
                                "⚡ Solides usuels : prisme, pyramide, cône, sphère",
                                "📐 Calculs de surfaces et de volumes"
                            )
                        }
                    }
                } else if (lvl == "Première") {
                    when (chNum) {
                        1 -> {
                            title = "Chapitre 1 : Équations & Second Degré"
                            summary = "Résolution d'équations de la forme ax² + bx + c = 0 à l'aide du discriminant Δ."
                            keyPoints = listOf(
                                "📌 Forme canonique du trinôme du second degré",
                                "💡 Calcul du discriminant Δ = b² - 4ac",
                                "⚡ Signe du trinôme et résolution d'inéquations",
                                "📐 Factorisation et somme/produit des racines"
                            )
                        }
                        2 -> {
                            title = "Chapitre 2 : Dérivation & Variations"
                            summary = "Définir le taux de variation, le nombre dérivé et l'équation de la tangente."
                            keyPoints = listOf(
                                "📌 Nombre dérivé f'(x₀) comme pente de la tangente",
                                "💡 Équation de la tangente : y = f'(x₀)(x - x₀) + f(x₀)",
                                "⚡ Formules de dérivation des fonctions usuelles (xⁿ, 1/x, √x)",
                                "📐 Lien entre signe de f'(x) et sens de variation de f(x)"
                            )
                        }
                        3 -> {
                            title = "Chapitre 3 : Suites Numériques"
                            summary = "Étude des suites, modes de génération, croissance, suites arithmétiques et géométriques."
                            keyPoints = listOf(
                                "📌 Suite définie par récurrence ou formule explicite",
                                "💡 Suite Arithmétique : u_(n+1) = u_n + r",
                                "⚡ Suite Géométrique : u_(n+1) = q * u_n",
                                "📐 Somme des termes d'une suite arithmétique ou géométrique"
                            )
                        }
                        4 -> {
                            title = "Chapitre 4 : Limites de Fonctions"
                            summary = "Comprendre le comportement d'une fonction aux bornes de son domaine d'étude."
                            keyPoints = listOf(
                                "📌 Limites infinies en un point réel (asymptotes verticales)",
                                "💡 Limites en ±∞ (asymptotes horizontales)",
                                "⚡ Théorèmes de comparaison (théorème des gendarmes)",
                                "📐 Résolution de formes indéterminées simples"
                            )
                        }
                        5 -> {
                            title = "Chapitre 5 : Produit Scalaire dans le Plan"
                            summary = "Définitions du produit scalaire, orthogonalité et applications géométriques."
                            keyPoints = listOf(
                                "📌 u . v = ||u|| * ||v|| * cos(u, v)",
                                "💡 Expression analytique dans un repère orthonormé : xx' + yy'",
                                "⚡ Propriétés : bilinéarité et symétrie",
                                "📐 Théorème d'Al-Kashi et équation de cercle"
                            )
                        }
                        6 -> {
                            title = "Chapitre 6 : Barycentre de Points Pondérés"
                            summary = "Définir le barycentre de 2, 3 ou 4 points et utiliser ses propriétés pour les démonstrations."
                            keyPoints = listOf(
                                "📌 Relation de définition : a.GA + b.GB = 0",
                                "💡 Propriété de réduction vectorielle",
                                "⚡ Coordonnées du barycentre dans un repère",
                                "📐 Associativité du barycentre (barycentres partiels)"
                            )
                        }
                        7 -> {
                            title = "Chapitre 7 : Trigonométrie & Équations Circulaires"
                            summary = "Formules d'addition, de duplication et résolution d'équations trigonométriques."
                            keyPoints = listOf(
                                "📌 Formules d'addition : cos(a+b) et sin(a+b)",
                                "💡 Formules de duplication : cos(2a) et sin(2a)",
                                "⚡ Équations de type cos(x) = cos(α) et sin(x) = sin(α)",
                                "📐 Représentation des solutions sur le cercle"
                            )
                        }
                        else -> {
                            title = "Chapitre 8 : Probabilités Fondamentales"
                            summary = "Modélisation d'une expérience aléatoire, calcul de probabilités simples et variables aléatoires."
                            keyPoints = listOf(
                                "📌 Univers Ω, événements et équiprobabilité",
                                "💡 Probabilité de l'union : P(A ∪ B) = P(A) + P(B) - P(A ∩ B)",
                                "⚡ Variable aléatoire et loi de probabilité",
                                "📐 Espérance mathématique, variance et écart-type"
                            )
                        }
                    }
                }
            }
            "pc" -> {
                if (lvl == "Seconde") {
                    when (chNum) {
                        1 -> {
                            title = "Chapitre 1 : Mouvement & Vitesse"
                            summary = "Définir la position, la trajectoire et calculer la vitesse d'un corps."
                            keyPoints = listOf(
                                "📌 Référentiels terrestres, géocentriques et héliocentriques",
                                "💡 Trajectoire d'un point mobile",
                                "⚡ Vitesse moyenne et instantanée",
                                "📐 Représentation des vecteurs vitesses"
                            )
                        }
                        2 -> {
                            title = "Chapitre 2 : Forces & Équilibres"
                            summary = "Modélisation des actions mécaniques par des forces et équilibres d'un solide."
                            keyPoints = listOf(
                                "📌 Définition d'une force et sa représentation",
                                "💡 Force de gravitation universelle",
                                "⚡ Loi de l'inertie ou premier principe de Newton",
                                "📐 Condition d'équilibre d'un solide"
                            )
                        }
                        else -> {
                            title = "Chapitre $chNum : Chimie & Électricité Seconde"
                            summary = "Découvrir la structure de la matière, le courant électrique et les calculs chimiques."
                            keyPoints = listOf(
                                "📌 Structure microscopique des atomes et molécules",
                                "💡 La quantité de matière et constante d'Avogadro",
                                "⚡ Circuits électriques en courant continu",
                                "📐 Bilans de matière pour une réaction chimique"
                            )
                        }
                    }
                } else if (lvl == "Première") {
                    when (chNum) {
                        1 -> {
                            title = "Chapitre 1 : Énergie Cinétique & Travail"
                            summary = "Comprendre les transferts d'énergie mécanique et le travail d'une force."
                            keyPoints = listOf(
                                "📌 Travail d'une force constante lors d'un déplacement",
                                "💡 Théorème de l'énergie cinétique",
                                "⚡ Puissance mécanique moyenne et instantanée",
                                "📐 Force motrice et force résistante"
                            )
                        }
                        2 -> {
                            title = "Chapitre 2 : Oxydoréduction"
                            summary = "Échanges d'électrons entre oxydants et réducteurs."
                            keyPoints = listOf(
                                "📌 Couple Ox/Red et nombre d'oxydation",
                                "💡 Ajustement de demi-équations d'oxydoréduction",
                                "⚡ Équation-bilan des réactions d'oxydoréduction",
                                "📐 Applications concrètes de la vie courante"
                            )
                        }
                        else -> {
                            title = "Chapitre $chNum : Énergie Potentielle, Électrostatique & Chimie Première"
                            summary = "Étudier les forces conservatives, l'électrostatique et la nomenclature de chimie organique."
                            keyPoints = listOf(
                                "📌 Énergie potentielle de pesanteur et énergie mécanique",
                                "💡 Loi de Coulomb et champ électrique",
                                "⚡ Nomenclature des alcanes, alcènes et alcools",
                                "📐 Dosage acido-basique et pH-métrie"
                            )
                        }
                    }
                }
            }
            "svt" -> {
                if (lvl == "Seconde") {
                    title = "Chapitre $chNum : Sciences de la Terre & Biologie Seconde"
                    summary = "Étudier l'organisme en activité, la structure de la Terre et la cellule vivante."
                    keyPoints = listOf(
                        "📌 Organisation générale de l'organisme humain",
                        "💡 La cellule, unité fondamentale structurale du vivant",
                        "⚡ La photosynthèse des plantes vertes",
                        "📐 Érosion et géologie externe de la Côte d'Ivoire"
                    )
                } else if (lvl == "Première") {
                    title = "Chapitre $chNum : Tectonique des Plaques, Cycle Cellulaire & Régulation"
                    summary = "Comprendre la dynamique interne de la Terre, la division cellulaire et la régulation glycémique."
                    keyPoints = listOf(
                        "📌 Modèle de la tectonique des plaques et dérives",
                        "💡 Division cellulaire (mitose) et réplication de l'ADN",
                        "⚡ Rôle du système nerveux dans la réception sensorielle",
                        "📐 Régulation de la glycémie et insuline"
                    )
                }
            }
            "philo" -> {
                title = "Chapitre $chNum : Éveil Philosophique & Méthodologie"
                summary = "Introduction à la pensée réflexive et à la préparation des épreuves de dissertation."
                keyPoints = listOf(
                    "📌 Distinction entre opinion commune et réflexion philosophique",
                    "💡 Les grands thèmes de la morale et de la liberté humaine",
                    "⚡ Méthodologie pas à pas de l'introduction rédigée",
                    "📐 Analyse de citations célèbres d'auteurs africains et occidentaux"
                )
            }
        }

        // Generate adaptive quiz questions to match
        quizQuestions = getAdaptiveQuizQuestions(ch.subjectId, lvl, chNum)

        // Generate custom fullContent for Seconde/Première
        val contentBuilder = StringBuilder()
        contentBuilder.append("# 📘 $title\n\n")
        contentBuilder.append("---\n\n")
        contentBuilder.append("## Niveau : $lvl | Série $srs\n\n")
        contentBuilder.append("### 📝 Introduction & Résumé du Cours\n")
        contentBuilder.append("$summary\n\n")
        contentBuilder.append("---\n\n")
        contentBuilder.append("### 🎯 Points clés à maîtriser :\n")
        keyPoints.forEach { kp ->
            contentBuilder.append("* $kp\n")
        }
        contentBuilder.append("\n---\n\n")
        contentBuilder.append("### 💡 Conseils pratiques d'assimilation :\n")
        contentBuilder.append("1. Relisez régulièrement vos fiches.\n")
        contentBuilder.append("2. Pratiquez avec les questions interactives de l'onglet **Quiz**.\n")
        contentBuilder.append("3. Posez vos questions de blocage directement à **EduBot** dans l'onglet tuteur IA !\n")
        fullContent = contentBuilder.toString()

        return ch.copy(
            title = title,
            level = lvl,
            series = srs,
            summary = summary,
            keyPoints = keyPoints,
            fullContent = fullContent,
            quizQuestions = quizQuestions
        )
    }

    private fun getAdaptiveQuizQuestions(subjectId: String, lvl: String, chNum: Int): List<QuizQuestion> {
        return when (subjectId) {
            "math" -> {
                if (lvl == "Seconde") {
                    when (chNum) {
                        1 -> listOf(
                            QuizQuestion("q1", "Parmi ces ensembles, quel est le plus petit contenant le nombre -1/3 ?", listOf("N (Entiers naturels)", "Z (Entiers relatifs)", "Q (Rationnels)", "D (Décimaux)"), 2, "Le nombre -1/3 est un rationnel non décimal périodique non fini, son plus petit ensemble d'appartenance est l'ensemble Q des rationnels."),
                            QuizQuestion("q2", "Quelle est l'intersection des intervalles [-3; 2] et ]0; 5] ?", listOf("[-3; 5]", "]0; 2]", "[-3; 0]", "[2; 5]"), 1, "L'intersection correspond aux éléments communs, soit les réels strictement supérieurs à 0 et inférieurs ou égaux à 2, c'est-à-dire ]0; 2].")
                        )
                        2 -> listOf(
                            QuizQuestion("q1", "Si f est une fonction paire, sa courbe représentative présente une symétrie par rapport à :", listOf("L'origine du repère", "L'axe des ordonnées", "L'axe des abscisses", "La droite y = x"), 1, "Une fonction paire vérifie f(-x) = f(x). Sa courbe est symétrique par rapport à l'axe des ordonnées (Oy)."),
                            QuizQuestion("q2", "Quel est l'antécédent de 4 par la fonction f(x) = 2x - 2 ?", listOf("6", "3", "4", "2"), 1, "On résout 2x - 2 = 4 => 2x = 6 => x = 3.")
                        )
                        else -> listOf(
                            QuizQuestion("q1", "Quelle est l'expression d'une fonction affine ?", listOf("f(x) = ax² + b", "f(x) = ax + b", "f(x) = a/x", "f(x) = √x"), 1, "Une fonction affine s'écrit sous la forme f(x) = ax + b pour tous réels a et b."),
                            QuizQuestion("q2", "La fonction inverse f(x) = 1/x est strictement décroissante sur :", listOf("R tout entier", "R*+ et R*- séparément", "R+", "R*+ uniquement"), 1, "La fonction inverse est strictement décroissante sur ]-∞, 0[ et sur ]0, +∞[ séparément. Elle n'est pas définie en 0.")
                        )
                    }
                } else { // Première
                    when (chNum) {
                        1 -> listOf(
                            QuizQuestion("q1", "Quel est le nombre de solutions réelles de l'équation ax² + bx + c = 0 si le discriminant Δ est strictement positif ?", listOf("Aucune solution", "Une solution double", "Deux solutions distinctes", "Trois solutions"), 2, "Si Δ > 0, le trinôme possède deux solutions réelles distinctes calculées par (-b ± √Δ) / 2a."),
                            QuizQuestion("q2", "Quelle est la forme canonique de x² - 4x + 5 ?", listOf("(x-2)² + 1", "(x-2)² - 1", "(x+2)² + 1", "(x-4)² + 5"), 0, "x² - 4x + 5 = (x - 2)² - 4 + 5 = (x - 2)² + 1.")
                        )
                        2 -> listOf(
                            QuizQuestion("q1", "Quelle est la dérivée de la fonction f(x) = x³ ?", listOf("f'(x) = 3x", "f'(x) = 3x²", "f'(x) = 2x²", "f'(x) = x²/3"), 1, "La formule de dérivation de xⁿ est n.xⁿ⁻¹. Pour n=3, on obtient 3x²."),
                            QuizQuestion("q2", "Si f'(x) est strictement positive sur un intervalle I, alors la fonction f y est :", listOf("Constante", "Strictement décroissante", "Strictement croissante", "Nulle"), 2, "Une dérivée strictement positive implique que la fonction f est strictement croissante sur cet intervalle.")
                        )
                        else -> listOf(
                            QuizQuestion("q1", "Quelle est la formule générale du terme u_n d'une suite arithmétique de premier terme u_0 et de raison r ?", listOf("u_n = u_0 * r^n", "u_n = u_0 + n.r", "u_n = u_0 - n.r", "u_n = u_0 + r^n"), 1, "Le terme général d'une suite arithmétique est u_n = u_0 + n.r."),
                            QuizQuestion("q2", "Quelle est la somme des termes de 1 à n (1 + 2 + ... + n) ?", listOf("n(n+1)/2", "n(n-1)/2", "n²/2", "n(n+1)"), 0, "La somme des n premiers entiers naturels non nuls vaut n(n+1)/2.")
                        )
                    }
                }
            }
            "pc" -> {
                if (lvl == "Seconde") {
                    listOf(
                        QuizQuestion("q1", "Un mouvement dont la trajectoire est une droite et la vitesse est constante est qualifié de :", listOf("Rectiligne uniforme", "Rectiligne accéléré", "Circulaire uniforme", "Curviligne uniforme"), 0, "Rectiligne désigne une trajectoire droite, et uniforme désigne une vitesse constante."),
                        QuizQuestion("q2", "Quelle est la formule de la quantité de matière n en fonction de la masse m et de la masse molaire M ?", listOf("n = m * M", "n = m / M", "n = M / m", "n = m + M"), 1, "La quantité de matière (en mol) est donnée par n = m / M.")
                    )
                } else {
                    listOf(
                        QuizQuestion("q1", "Quelle est l'unité internationale de l'énergie et du travail ?", listOf("Watt (W)", "Newton (N)", "Joule (J)", "Volt (V)"), 2, "L'énergie et le travail d'une force s'expriment en Joules (J) dans le Système International."),
                        QuizQuestion("q2", "Lors d'une réaction d'oxydoréduction, l'oxydant est une espèce chimique qui :", listOf("Perd des électrons", "Gagne des électrons", "Perd des protons", "Gagne des protons"), 1, "Un oxydant gagne/capte un ou plusieurs électrons.")
                    )
                }
            }
            else -> {
                listOf(
                    QuizQuestion("q1", "Comment appelle-t-on le fait d'apprendre régulièrement ?", listOf("L'inconstance", "La révision active", "La procrastination", "Le bachotage"), 1, "La révision active et régulière garantit la mémorisation à long terme."),
                    QuizQuestion("q2", "Quel facteur favorise le plus la réussite scolaire ?", listOf("La chance", "Le travail régulier", "L'attente du dernier jour", "L'absence de sommeil"), 1, "Le travail régulier et structuré est le pilier de la réussite.")
                )
            }
        }
    }

    fun signOutStudent() {
        authManager.signOut()
    }
}
