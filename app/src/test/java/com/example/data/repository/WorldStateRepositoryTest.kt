package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ShadowBlueDatabase
import com.example.data.model.CompanionIds
import com.example.data.model.KittenIds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorldStateRepositoryTest {

    private lateinit var db: ShadowBlueDatabase
    private lateinit var repository: WorldStateRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShadowBlueDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorldStateRepository(db.worldStateDao(), db.journalDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun ensureAndSaveWorldState_persistsOfflineAcrossReads() = runBlocking {
        val initial = repository.ensureWorldStateExists()
        assertEquals(1L, initial.currentChapter)

        val updated = initial.copy(
            currentChapter = 4L,
            highestUnlockedChapter = 5L,
            timeOfDay = 0.25,
            unlockedCompanions = listOf(CompanionIds.OWL, CompanionIds.FOX, CompanionIds.MOTH),
            rescuedKittens = listOf(KittenIds.SOOT, KittenIds.PEBBLE, KittenIds.CLOVER, KittenIds.BLUE),
            activatedLanternIds = listOf("c1_l1", "c4_l4"),
            collectedFireflyIds = listOf("c1_f1", "c4_f6"),
            totalFirefliesCollected = 2L,
            storyCompleted = true,
            sequelUnlocked = true
        )
        repository.saveWorldState(updated)

        val observed = repository.observeWorldState().first { it != null }
        assertEquals(4L, observed?.currentChapter)
        assertEquals(5L, observed?.highestUnlockedChapter)
        assertTrue(observed?.storyCompleted == true)
        assertTrue(observed?.sequelUnlocked == true)
        assertTrue(observed?.rescuedKittens?.contains(KittenIds.BLUE) == true)
    }

    @Test
    fun recordJournalEntry_persistsAndEmitsInOrder() = runBlocking {
        repository.recordJournalEntry(
            entryId = "kit_blue",
            chapter = 4L,
            category = "ending",
            title = "Reunited with Blue",
            speaker = "Blue",
            summary = "Found Blue beneath the moonlit stone garden gate."
        )

        val entries = repository.observeJournalEntries().first { it.isNotEmpty() }
        assertEquals(1, entries.size)
        assertEquals("Reunited with Blue", entries.first().title)
    }
}
