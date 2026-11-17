package com.example.data.model

data class WorldState(
    val id: String = "main",
    val currentChapter: Long = 1L,
    val highestUnlockedChapter: Long = 1L,
    val timeOfDay: Double = 0.78, // Start at cozy dusk-twilight
    val unlockedCompanions: List<String> = emptyList(),
    val rescuedKittens: List<String> = emptyList(),
    val activatedLanternIds: List<String> = emptyList(),
    val collectedFireflyIds: List<String> = emptyList(),
    val lastCheckpointX: Double = 90.0,
    val totalFirefliesCollected: Long = 0L,
    val storyCompleted: Boolean = false,
    val sequelUnlocked: Boolean = false
)

object CompanionIds {
    const val OWL = "owl"   // Pip: Moonlight Glide
    const val FOX = "fox"   // Bramble: Shadow Dash
    const val MOTH = "moth" // Luma: Lantern Pulse / Flutter Double Jump
}

object KittenIds {
    const val SOOT = "soot"
    const val PEBBLE = "pebble"
    const val CLOVER = "clover"
    const val BLUE = "blue"
}
