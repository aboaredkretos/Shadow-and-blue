package com.example.data.repository

import android.content.Context
import com.example.data.local.JournalDao
import com.example.data.local.JournalEntryEntity
import com.example.data.local.ShadowBlueDatabase
import com.example.data.local.WorldStateDao
import com.example.data.local.WorldStateEntity
import com.example.data.model.JournalEntry
import com.example.data.model.WorldState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorldStateRepository(
    private val worldStateDao: WorldStateDao,
    private val journalDao: JournalDao
) {
    constructor(context: Context) : this(
        ShadowBlueDatabase.getInstance(context).worldStateDao(),
        ShadowBlueDatabase.getInstance(context).journalDao()
    )

    fun observeWorldState(): Flow<WorldState?> =
        worldStateDao.observeWorldState().map { it?.toDomain() }

    suspend fun getWorldState(): WorldState? =
        worldStateDao.getWorldState()?.toDomain()

    suspend fun ensureWorldStateExists(): WorldState {
        val existing = getWorldState()
        if (existing != null) return existing
        val initial = WorldState()
        worldStateDao.upsertWorldState(WorldStateEntity.fromDomain(initial))
        return initial
    }

    suspend fun saveWorldState(state: WorldState) {
        val clampedTime = state.timeOfDay.coerceIn(0.0, 1.0)
        val clampedChapter = state.currentChapter.coerceIn(1L, 5L)
        val clampedHighest = state.highestUnlockedChapter.coerceIn(clampedChapter, 5L)
        val clampedCheckpointX = state.lastCheckpointX.coerceIn(0.0, 50000.0)
        val clampedFireflies = state.totalFirefliesCollected.coerceIn(0L, 1000L)

        val sanitized = state.copy(
            id = "main",
            currentChapter = clampedChapter,
            highestUnlockedChapter = clampedHighest,
            timeOfDay = clampedTime,
            unlockedCompanions = state.unlockedCompanions.distinct(),
            rescuedKittens = state.rescuedKittens.distinct(),
            activatedLanternIds = state.activatedLanternIds.distinct(),
            collectedFireflyIds = state.collectedFireflyIds.distinct(),
            lastCheckpointX = clampedCheckpointX,
            totalFirefliesCollected = clampedFireflies
        )
        worldStateDao.upsertWorldState(WorldStateEntity.fromDomain(sanitized))
    }

    fun observeJournalEntries(): Flow<List<JournalEntry>> =
        journalDao.observeJournalEntries().map { list -> list.map { it.toDomain() } }

    suspend fun recordJournalEntry(
        entryId: String,
        chapter: Long,
        category: String,
        title: String,
        speaker: String,
        summary: String
    ) {
        journalDao.upsertEntry(
            JournalEntryEntity(
                id = entryId,
                chapter = chapter.coerceIn(1L, 5L),
                category = category,
                title = title,
                speaker = speaker,
                summary = summary,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }
}
