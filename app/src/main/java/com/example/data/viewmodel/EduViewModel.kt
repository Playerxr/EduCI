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

    // EduBot Chat State
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "1",
                sender = "bot",
                text = "Bonjour ! Je suis EduBot, ton tuteur IA pour le BAC Ivoiriens 🇨🇮. Quelle matière ou quel chapitre veux-tu réviser ensemble aujourd'hui ?"
            )
        )
    )
    val chatInputText = MutableStateFlow("")
    val isBotThinking = MutableStateFlow(false)

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
        // Quick anti-cheating check for direct cheating requests
        val promptLower = userPrompt.lowercase()
        val isCheatingAttempt = promptLower.contains("triche") || 
                promptLower.contains("donne moi la reponse de mon devoir") ||
                promptLower.contains("fais mon devoir a ma place") ||
                promptLower.contains("resolvant mon examen en direct")

        if (isCheatingAttempt) {
            return "🛡️ **Garde-fou Anti-Tricherie EduBot** :\n\nEn tant qu'assistant pédagogique conforme au système éducatif ivoirien, je ne résous pas les devoirs à votre place et ne fournis pas de réponses pour tricher aux épreuves. \n\nEn revanche, je peux vous expliquer la **méthode de résolution** et les **notions du cours** pour que vous réussissiez le BAC par vos propres compétences !"
        }

        if (isOfflineMode.value) {
            val offlineResponse = getOfflineLocalReply(userPrompt)
            return "🔌 **[Mode Hors-ligne Actif]** Réponse locale de la base de données EduCI (0% Data) :\n\n$offlineResponse"
        }

        try {
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val sysInstruction = """
                    Tu es EduBot CI, l'Assistant Pédagogique IA Officiel pour les élèves du Second Cycle (Seconde, Première, Terminale) et du BAC en Côte d'Ivoire.
                    
                    RÈGLE D'OR & GARDE-FOU ANTI-TRICHERIE :
                    - Tu as l'interdiction stricte d'aider à la tricherie scolaire (ne jamais fournir de réponses prêtes à copier lors d'un devoir surveillé, une composition ou un examen, et ne jamais rédiger une évaluation à la place de l'élève).
                    - Si la question de l'élève demande de tricher ou de faire son devoir à sa place, réponds gentiment : "En tant qu'assistant pédagogique conforme au Ministère de l'Éducation Nationale de Côte d'Ivoire (MENA), je ne résous pas les devoirs à votre place. Voici la méthode et les explications pour vous guider :".
                    - Sois clair, concis, motivant, structuré en Markdown et centré sur l'apprentissage actif du programme ivoirien.
                """.trimIndent()
                
                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$sysInstruction\n\nQuestion de l'élève : $userPrompt"))
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
        return getOfflineLocalReply(userPrompt)
    }

    private fun getOfflineLocalReply(userPrompt: String): String {
        return when {
            userPrompt.contains("methode", ignoreCase = true) || userPrompt.contains("dissertation", ignoreCase = true) ->
                "Pour réussir la dissertation littéraire ou philosophique au BAC CI :\n\n1. **Introduction** : Amorce (contexte), Définition des termes clés du sujet, Problématique, Annonce claire du plan.\n2. **Développement** : 2 ou 3 parties équilibrées étayées par des arguments solides et des citations d'auteurs (ex: Bernard Dadié, Ahmadou Kourouma, Jean-Paul Sartre, Karl Marx).\n3. **Conclusion** : Bilan synthétique qui répond à la problématique sans introduire de nouveaux éléments, puis ouverture."

            userPrompt.contains("tvi", ignoreCase = true) || userPrompt.contains("math", ignoreCase = true) ->
                "En Mathématiques (Terminale D/C) : Le Théorème des Valeurs Intermédiaires (TVI) nécessite 2 conditions indispensables :\n1. La fonction f doit être continue sur l'intervalle [a, b].\n2. Le nombre k doit être compris entre f(a) et f(b).\n\n💡 *Astuce BAC* : Pour prouver l'existence d'une unique solution α, vous devez impérativement justifier que la fonction f est strictement monotone (uniquement croissante ou uniquement décroissante) sur cet intervalle !"

            userPrompt.contains("bac", ignoreCase = true) || userPrompt.contains("examen", ignoreCase = true) ->
                "Le secret de la mention au BAC ivoirien 🇨🇮 repose sur :\n- Un entraînement régulier avec les annales et anciennes épreuves (2020-2025).\n- La maîtrise des formules de Physique-Chimie et théorèmes de Mathématiques.\n- Les fiches de révisions condensées d'EduCI.\n- L'autodiscipline et des sessions d'études quotidiennes d'au moins 2 heures."

            else ->
                "C'est une excellente question sur le programme scolaire ivoirien ! Comme nous sommes hors-ligne ou que la connexion est limitée, n'hésitez pas à consulter les fiches de cours détaillées (de l'onglet **Cours**) et à tester vos connaissances avec le **Quiz Chrono** pour valider chaque notion."
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
