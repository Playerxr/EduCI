package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val chapterId: String,
    val chapterTitle: String,
    val subjectId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: String,
    val subjectName: String,
    val noteTitle: String,
    val noteContent: String,
    val createdAt: Long = System.currentTimeMillis()
)
