package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val description: String,
    val isSelected: Boolean = true,
    val levelListCsv: String = "Seconde,Première,Terminale",
    val seriesListCsv: String = "A,C,D"
)

fun SubjectEntity.toSubject(): Subject = Subject(
    id = id,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    description = description,
    isSelected = isSelected,
    levelList = if (levelListCsv.isBlank()) listOf("Seconde", "Première", "Terminale") else levelListCsv.split(",").map { it.trim() },
    seriesList = if (seriesListCsv.isBlank()) listOf("A", "C", "D") else seriesListCsv.split(",").map { it.trim() }
)

data class Subject(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val description: String,
    val isSelected: Boolean = true,
    val levelList: List<String> = listOf("Seconde", "Première", "Terminale"),
    val seriesList: List<String> = listOf("A", "C", "D")
) {
    fun toEntity(): SubjectEntity = SubjectEntity(
        id = id,
        name = name,
        iconName = iconName,
        colorHex = colorHex,
        description = description,
        isSelected = isSelected,
        levelListCsv = levelList.joinToString(","),
        seriesListCsv = seriesList.joinToString(",")
    )
}

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val chapterNumber: Int = 1,
    val title: String,
    val level: String,
    val series: String,
    val summary: String,
    val keyPointsJson: String,
    val fullContent: String,
    val formulaSheet: String? = null,
    val bacExamTipsJson: String = "[]",
    val quizQuestionsJson: String = "[]",
    val isCompleted: Boolean = false
)

fun ChapterEntity.toChapter(): Chapter {
    val keyPointsList = try {
        val jsonArray = JSONArray(keyPointsJson)
        (0 until jsonArray.length()).map { jsonArray.getString(it) }
    } catch (e: Exception) {
        if (keyPointsJson.isNotBlank()) keyPointsJson.split("||").map { it.trim() } else emptyList()
    }

    val bacTipsList = try {
        val jsonArray = JSONArray(bacExamTipsJson)
        (0 until jsonArray.length()).map { jsonArray.getString(it) }
    } catch (e: Exception) {
        if (bacExamTipsJson.isNotBlank()) bacExamTipsJson.split("||").map { it.trim() } else emptyList()
    }

    val questionsList = try {
        val jsonArray = JSONArray(quizQuestionsJson)
        val list = mutableListOf<QuizQuestion>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val optsArr = obj.getJSONArray("options")
            val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
            list.add(
                QuizQuestion(
                    id = obj.optString("id", "q_$i"),
                    question = obj.optString("question"),
                    options = opts,
                    correctAnswerIndex = obj.optInt("correctAnswerIndex", 0),
                    explanation = obj.optString("explanation")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }

    return Chapter(
        id = id,
        subjectId = subjectId,
        chapterNumber = chapterNumber,
        title = title,
        level = level,
        series = series,
        summary = summary,
        keyPoints = keyPointsList,
        fullContent = fullContent,
        formulaSheet = formulaSheet,
        bacExamTips = bacTipsList,
        quizQuestions = questionsList
    )
}

data class Chapter(
    val id: String,
    val subjectId: String,
    val chapterNumber: Int = 1,
    val title: String,
    val level: String,
    val series: String,
    val summary: String,
    val keyPoints: List<String>,
    val fullContent: String,
    val formulaSheet: String? = null,
    val bacExamTips: List<String> = emptyList(),
    val quizQuestions: List<QuizQuestion> = emptyList()
) {
    fun toEntity(): ChapterEntity {
        val keyPointsArr = JSONArray(keyPoints)
        val bacTipsArr = JSONArray(bacExamTips)

        val questionsArr = JSONArray()
        quizQuestions.forEach { q ->
            val obj = JSONObject().apply {
                put("id", q.id)
                put("question", q.question)
                put("options", JSONArray(q.options))
                put("correctAnswerIndex", q.correctAnswerIndex)
                put("explanation", q.explanation)
            }
            questionsArr.put(obj)
        }

        return ChapterEntity(
            id = id,
            subjectId = subjectId,
            chapterNumber = chapterNumber,
            title = title,
            level = level,
            series = series,
            summary = summary,
            keyPointsJson = keyPointsArr.toString(),
            fullContent = fullContent,
            formulaSheet = formulaSheet,
            bacExamTipsJson = bacTipsArr.toString(),
            quizQuestionsJson = questionsArr.toString()
        )
    }
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "bot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val chapterId: String,
    val subjectId: String,
    val chapterTitle: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val chapterTitle: String,
    val noteTitle: String,
    val noteContent: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val matricule: String = "",
    val selectedLevel: String = "Terminale",
    val selectedSeries: String = "D",
    val schoolName: String = "",
    val examTarget: String = "",
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val coins: Int = 100,
    val isOnboardingCompleted: Boolean = false
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterTitle: String,
    val level: String,
    val series: String,
    val studentName: String,
    val scorePercentage: Int,
    val issueDate: String,
    val certificateCode: String
)

@Entity(tableName = "completed_chapters")
data class CompletedChapterEntity(
    @PrimaryKey val chapterId: String,
    val subjectId: String,
    val completedAt: Long = System.currentTimeMillis(),
    val quizHighScore: Int = 0
)

@Entity(tableName = "historique")
data class HistoriqueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterTitle: String,
    val score: Int,
    val totalQuestions: Int = 5,
    val scorePercentage: Int,
    val revisionDate: Long = System.currentTimeMillis(),
    val revisionType: String = "QUIZ", // QUIZ, REVISION, BAC_BLANC
    val durationSeconds: Int = 90,
    val feedback: String = ""
)

data class EducationalIllustration(
    val id: String,
    val subjectId: String,
    val title: String,
    val category: String,
    val description: String,
    val keyPoints: List<String>,
    val defaultWebQuery: String,
    val webFallbackUrl: String
)


