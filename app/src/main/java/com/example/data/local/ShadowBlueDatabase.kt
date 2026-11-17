package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.example.data.model.JournalEntry
import com.example.data.model.WorldState
import kotlinx.coroutines.flow.Flow

class StringListConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String =
        list.filter { it.isNotBlank() }.joinToString(separator = "|")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("|").filter { it.isNotBlank() }
}

@Entity(tableName = "world_state")
data class WorldStateEntity(
    @PrimaryKey val id: String = "main",
    val currentChapter: Long = 1L,
    val highestUnlockedChapter: Long = 1L,
    val timeOfDay: Double = 0.78,
    val unlockedCompanions: List<String> = emptyList(),
    val rescuedKittens: List<String> = emptyList(),
    val activatedLanternIds: List<String> = emptyList(),
    val collectedFireflyIds: List<String> = emptyList(),
    val lastCheckpointX: Double = 90.0,
    val totalFirefliesCollected: Long = 0L,
    val storyCompleted: Boolean = false,
    val sequelUnlocked: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): WorldState = WorldState(
        id = id,
        currentChapter = currentChapter,
        highestUnlockedChapter = highestUnlockedChapter,
        timeOfDay = timeOfDay,
        unlockedCompanions = unlockedCompanions,
        rescuedKittens = rescuedKittens,
        activatedLanternIds = activatedLanternIds,
        collectedFireflyIds = collectedFireflyIds,
        lastCheckpointX = lastCheckpointX,
        totalFirefliesCollected = totalFirefliesCollected,
        storyCompleted = storyCompleted,
        sequelUnlocked = sequelUnlocked
    )

    companion object {
        fun fromDomain(state: WorldState): WorldStateEntity = WorldStateEntity(
            id = state.id.ifBlank { "main" },
            currentChapter = state.currentChapter,
            highestUnlockedChapter = state.highestUnlockedChapter,
            timeOfDay = state.timeOfDay,
            unlockedCompanions = state.unlockedCompanions,
            rescuedKittens = state.rescuedKittens,
            activatedLanternIds = state.activatedLanternIds,
            collectedFireflyIds = state.collectedFireflyIds,
            lastCheckpointX = state.lastCheckpointX,
            totalFirefliesCollected = state.totalFirefliesCollected,
            storyCompleted = state.storyCompleted,
            sequelUnlocked = state.sequelUnlocked,
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val chapter: Long,
    val category: String,
    val title: String,
    val speaker: String,
    val summary: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): JournalEntry = JournalEntry(
        id = id,
        chapter = chapter,
        category = category,
        title = title,
        speaker = speaker,
        summary = summary,
        updatedAtMillis = updatedAtMillis
    )
}

@Dao
interface WorldStateDao {
    @Query("SELECT * FROM world_state WHERE id = 'main' LIMIT 1")
    fun observeWorldState(): Flow<WorldStateEntity?>

    @Query("SELECT * FROM world_state WHERE id = 'main' LIMIT 1")
    suspend fun getWorldState(): WorldStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorldState(entity: WorldStateEntity)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY chapter ASC, updatedAtMillis ASC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: JournalEntryEntity)
}

@Database(
    entities = [WorldStateEntity::class, JournalEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverters::class)
abstract class ShadowBlueDatabase : RoomDatabase() {
    abstract fun worldStateDao(): WorldStateDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: ShadowBlueDatabase? = null

        fun getInstance(context: Context): ShadowBlueDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShadowBlueDatabase::class.java,
                    "shadow_blue_world.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
