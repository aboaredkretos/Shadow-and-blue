package com.example.game

import com.example.data.model.WorldState

enum class SpeakerKind {
    SHADOW,
    OWL,
    FOX,
    MOTH,
    SOOT,
    PEBBLE,
    CLOVER,
    BLUE,
    NARRATOR
}

data class DialogueLine(
    val speakerName: String,
    val speakerKind: SpeakerKind,
    val text: String
)

data class DialogueEvent(
    val id: String,
    val lines: List<DialogueLine>,
    val rewardCompanion: String? = null,
    val rewardKitten: String? = null,
    val triggersChapterComplete: Boolean = false,
    val triggersStoryFinale: Boolean = false,
    val journalTitle: String? = null,
    val journalCategory: String? = null
)

data class PlatformRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isBridge: Boolean = false,
    val isWall: Boolean = false
)

enum class HazardType {
    THORNS,
    WATER,
    FALLING_SPORE,
    WIND_GUST
}

data class HazardZone(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: HazardType,
    val cycleOffset: Float = 0f,
    val dropHeight: Float = 90f
)

data class LanternCheckpoint(
    val id: String,
    val x: Float,
    val y: Float,
    val loreLines: List<DialogueLine>? = null
)

data class FireflyCollectible(
    val id: String,
    val x: Float,
    val y: Float
)

data class CompanionNpc(
    val id: String,
    val name: String,
    val speakerKind: SpeakerKind,
    val abilityTitle: String,
    val abilityDesc: String,
    val x: Float,
    val y: Float,
    val dialogue: List<DialogueLine>
)

data class KittenGoal(
    val id: String,
    val name: String,
    val speakerKind: SpeakerKind,
    val eyeColorHex: Long,
    val x: Float,
    val y: Float,
    val dialogue: List<DialogueLine>,
    val isBlueFinale: Boolean = false
)

data class ChapterDefinition(
    val number: Int,
    val title: String,
    val subtitle: String,
    val hazardLabel: String,
    val worldWidth: Float,
    val spawnX: Float,
    val spawnY: Float,
    val initialTimeHint: Float,
    val introDialogue: List<DialogueLine>,
    val platforms: List<PlatformRect>,
    val hazards: List<HazardZone>,
    val lanterns: List<LanternCheckpoint>,
    val fireflies: List<FireflyCollectible>,
    val companion: CompanionNpc?,
    val kittenGoal: KittenGoal?,
    val isSequelPrologue: Boolean = false
)

enum class AppScreen {
    TITLE,
    PLAYING,
    CHAPTERS,
    JOURNAL,
    SEQUEL_EPILOGUE
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val colorHex: Long,
    val size: Float = 2.5f
)

data class DashGhost(
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    var life: Float
)

data class PlayerRuntimeState(
    val x: Float = 90f,
    val y: Float = 150f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val facingRight: Boolean = true,
    val onGround: Boolean = false,
    val isGliding: Boolean = false,
    val hasDoubleJump: Boolean = false,
    val dashTimer: Float = 0f,
    val dashCooldown: Float = 0f,
    val pulseTimer: Float = 0f,
    val pulseCooldown: Float = 0f,
    val invulnTimer: Float = 0f,
    val animTime: Float = 0f,
    val blinkTimer: Float = 2.5f,
    val blueCompanionX: Float = 55f,
    val blueCompanionY: Float = 154f
)

data class GameSessionUiState(
    val screen: AppScreen = AppScreen.TITLE,
    val worldState: WorldState = WorldState(),
    val activeChapter: ChapterDefinition? = null,
    val player: PlayerRuntimeState = PlayerRuntimeState(),
    val cameraX: Float = 0f,
    val timeOfDay: Float = 0.78f,
    val activeDialogue: DialogueEvent? = null,
    val dialogueLineIndex: Int = 0,
    val revealedChars: Int = 0,
    val chapterBannerTitle: String? = null,
    val abilityBanner: String? = null,
    val isMuted: Boolean = false,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)
