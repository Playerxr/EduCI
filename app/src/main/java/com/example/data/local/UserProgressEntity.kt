package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val chapterId: String,
    val isCompleted: Boolean = false,
    val quizScorePercent: Int = 0,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile_stats")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val selectedLevelCode: String = "Tle",
    val selectedSeriesCode: String = "SERIE_D",
    val totalXp: Int = 240,
    val streakDays: Int = 5,
    val lastActiveDate: String = ""
)
