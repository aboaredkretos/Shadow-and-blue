package com.example

import com.example.game.ChapterDefinitions
import com.example.ui.game.DayNightCycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {

    @Test
    fun chapters_includeBlueFinaleAndSequelPrologue() {
        val ch4 = ChapterDefinitions.getChapter(4)
        assertNotNull(ch4.kittenGoal)
        assertEquals("Blue", ch4.kittenGoal?.name)
        assertTrue(ch4.kittenGoal?.isBlueFinale == true)

        val ch5 = ChapterDefinitions.getChapter(5)
        assertTrue(ch5.isSequelPrologue)
        assertFalse(ch5.lanterns.isEmpty())
    }

    @Test
    fun dayNightCycle_transitionsAllFourPhases() {
        assertEquals("Midnight", DayNightCycle.evaluate(0.02f).phaseLabel)
        assertEquals("Dawn", DayNightCycle.evaluate(0.25f).phaseLabel)
        assertEquals("Daylight", DayNightCycle.evaluate(0.50f).phaseLabel)
        assertEquals("Dusk", DayNightCycle.evaluate(0.75f).phaseLabel)
    }
}
