package com.example.data.model

data class JournalEntry(
    val id: String = "",
    val chapter: Long = 1L,
    val category: String = "lore",
    val title: String = "",
    val speaker: String = "",
    val summary: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
