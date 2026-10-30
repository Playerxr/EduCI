package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.models.BookmarkEntity
import com.example.data.models.CertificateEntity
import com.example.data.models.ChapterEntity
import com.example.data.models.CompletedChapterEntity
import com.example.data.models.HistoriqueEntity
import com.example.data.models.NoteEntity
import com.example.data.models.SubjectEntity
import com.example.data.models.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoriqueDao {
    @Query("SELECT * FROM historique ORDER BY revisionDate DESC")
    fun getAllHistorique(): Flow<List<HistoriqueEntity>>

    @Query("SELECT * FROM historique WHERE subjectId = :subjectId ORDER BY revisionDate DESC")
    fun getHistoriqueBySubject(subjectId: String): Flow<List<HistoriqueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorique(item: HistoriqueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHistorique(items: List<HistoriqueEntity>)

    @Query("SELECT COUNT(*) FROM historique")
    suspend fun getHistoriqueCount(): Int

    @Query("DELETE FROM historique WHERE id = :id")
    suspend fun deleteHistoriqueById(id: Long)

    @Query("DELETE FROM historique")
    suspend fun clearAllHistorique()
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY chapterNumber ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    fun getChaptersBySubject(subjectId: String): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCount(): Int

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE chapterId = :chapterId")
    suspend fun deleteBookmarkByChapterId(chapterId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE chapterId = :chapterId)")
    fun isBookmarked(chapterId: String): Flow<Boolean>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM personal_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM personal_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates ORDER BY issueDate DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM certificates WHERE chapterId = :chapterId)")
    fun isChapterCertified(chapterId: String): Flow<Boolean>
}

@Dao
interface CompletedChapterDao {
    @Query("SELECT * FROM completed_chapters")
    fun getAllCompletedChapters(): Flow<List<CompletedChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markChapterCompleted(completed: CompletedChapterEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE isSelected = 1")
    fun getSelectedSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("UPDATE subjects SET isSelected = :isSelected WHERE id = :subjectId")
    suspend fun updateSubjectSelection(subjectId: String, isSelected: Boolean)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int
}

@Database(
    entities = [
        BookmarkEntity::class,
        NoteEntity::class,
        UserProfileEntity::class,
        CertificateEntity::class,
        CompletedChapterEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        HistoriqueEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class EduDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun certificateDao(): CertificateDao
    abstract fun completedChapterDao(): CompletedChapterDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun historiqueDao(): HistoriqueDao


    companion object {
        @Volatile
        private var INSTANCE: EduDatabase? = null

        fun getDatabase(context: Context): EduDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduDatabase::class.java,
                    "educi_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
