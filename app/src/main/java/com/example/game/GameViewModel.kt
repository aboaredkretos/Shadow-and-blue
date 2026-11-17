package com.example.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ForestAudioEngine
import com.example.data.model.CompanionIds
import com.example.data.model.JournalEntry
import com.example.data.model.WorldState
import com.example.data.repository.WorldStateRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

data class PlayerInputState(
    val left: Boolean = false,
    val right: Boolean = false,
    val jumpHeld: Boolean = false,
    val jumpPressedEdge: Boolean = false,
    val dashPressedEdge: Boolean = false,
    val pulsePressedEdge: Boolean = false
)

class GameViewModel(
    private val repository: WorldStateRepository,
    val audioEngine: ForestAudioEngine = ForestAudioEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSessionUiState())
    val uiState: StateFlow<GameSessionUiState> = _uiState.asStateFlow()

    val journalEntries: StateFlow<List<JournalEntry>> = repository.observeJournalEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _dashGhosts = MutableStateFlow<List<DashGhost>>(emptyList())
    val dashGhosts: StateFlow<List<DashGhost>> = _dashGhosts.asStateFlow()

    private var simClock = 0f
    private var coyoteTimer = 0f
    private var stepAudioCooldown = 0f
    private var lastSavedTimeOfDay = 0.78f
    private var pendingSaveJob: Job? = null
    private val triggeredEvents = mutableSetOf<String>()
    private var bannerJob: Job? = null
    private var typewriterJob: Job? = null

    init {
        audioEngine.start()
        viewModelScope.launch {
            try {
                val initial = repository.ensureWorldStateExists()
                _uiState.update {
                    it.copy(
                        worldState = initial,
                        timeOfDay = initial.timeOfDay.toFloat()
                    )
                }
                audioEngine.moodPhase = initial.timeOfDay
                lastSavedTimeOfDay = initial.timeOfDay.toFloat()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Failed to ensure local world state", e)
            }
        }

        viewModelScope.launch {
            repository.observeWorldState().collect { saved ->
                if (saved != null) {
                    _uiState.update { current ->
                        current.copy(worldState = saved)
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        audioEngine.isFinaleMood = screen == AppScreen.SEQUEL_EPILOGUE
        _uiState.update { it.copy(screen = screen) }
    }

    fun toggleMute() {
        val muted = audioEngine.toggleMute()
        _uiState.update { it.copy(isMuted = muted) }
    }

    fun startOrContinueChapter(chapterNumber: Int? = null, fromBeginningOfChapter: Boolean = false) {
        val ws = _uiState.value.worldState
        val targetChapterNum = chapterNumber ?: ws.currentChapter.toInt().coerceIn(1, 5)
        val chapter = ChapterDefinitions.getChapter(targetChapterNum)
        val spawnX = if (!fromBeginningOfChapter && targetChapterNum == ws.currentChapter.toInt()) {
            ws.lastCheckpointX.toFloat().coerceIn(40f, chapter.worldWidth - 120f)
        } else {
            chapter.spawnX
        }
        val spawnY = findGroundYAt(chapter, spawnX) - 16f

        val startTime = if (fromBeginningOfChapter) chapter.initialTimeHint else ws.timeOfDay.toFloat()
        audioEngine.moodPhase = startTime.toDouble()
        audioEngine.isFinaleMood = chapter.isSequelPrologue

        triggeredEvents.clear()
        _particles.value = emptyList()
        _dashGhosts.value = emptyList()

        val updatedWs = ws.copy(
            currentChapter = targetChapterNum.toLong(),
            lastCheckpointX = spawnX.toDouble()
        )
        _uiState.update {
            it.copy(
                screen = AppScreen.PLAYING,
                activeChapter = chapter,
                worldState = updatedWs,
                timeOfDay = startTime,
                player = PlayerRuntimeState(
                    x = spawnX,
                    y = spawnY,
                    onGround = true,
                    hasDoubleJump = updatedWs.unlockedCompanions.contains(CompanionIds.MOTH),
                    blueCompanionX = (spawnX - 28f).coerceAtLeast(20f),
                    blueCompanionY = spawnY + 4f
                ),
                cameraX = (spawnX - 100f).coerceAtLeast(0f),
                chapterBannerTitle = "Chapter ${chapter.number}: ${chapter.title}"
            )
        }
        persistWorldState(updatedWs)

        if (spawnX <= chapter.spawnX + 35f && chapter.introDialogue.isNotEmpty()) {
            triggerDialogue(
                DialogueEvent(
                    id = "intro_ch_${chapter.number}",
                    lines = chapter.introDialogue
                )
            )
        }
        showBanner("Chapter ${chapter.number} • ${chapter.title}")
    }

    private fun showBanner(text: String) {
        bannerJob?.cancel()
        _uiState.update { it.copy(chapterBannerTitle = text) }
        bannerJob = viewModelScope.launch {
            delay(3200L)
            _uiState.update { it.copy(chapterBannerTitle = null) }
        }
    }

    private fun showAbilityBanner(text: String) {
        _uiState.update { it.copy(abilityBanner = text) }
        viewModelScope.launch {
            delay(4200L)
            _uiState.update { it.copy(abilityBanner = null) }
        }
    }

    fun triggerDialogue(event: DialogueEvent) {
        if (triggeredEvents.contains(event.id)) return
        triggeredEvents.add(event.id)
        _uiState.update {
            it.copy(
                activeDialogue = event,
                dialogueLineIndex = 0,
                revealedChars = 0
            )
        }
        startTypewriter()
    }

    private fun startTypewriter() {
        typewriterJob?.cancel()
        typewriterJob = viewModelScope.launch {
            val event = _uiState.value.activeDialogue ?: return@launch
            val line = event.lines.getOrNull(_uiState.value.dialogueLineIndex) ?: return@launch
            val total = line.text.length
            for (i in 1..total) {
                delay(20L)
                _uiState.update { it.copy(revealedChars = i) }
                if (i % 4 == 1) audioEngine.playDialogueBlip()
            }
        }
    }

    fun advanceOrSkipDialogue() {
        val state = _uiState.value
        val event = state.activeDialogue ?: return
        val currentLine = event.lines.getOrNull(state.dialogueLineIndex) ?: return

        if (state.revealedChars < currentLine.text.length) {
            typewriterJob?.cancel()
            _uiState.update { it.copy(revealedChars = currentLine.text.length) }
            return
        }

        val nextIdx = state.dialogueLineIndex + 1
        if (nextIdx < event.lines.size) {
            _uiState.update { it.copy(dialogueLineIndex = nextIdx, revealedChars = 0) }
            startTypewriter()
            return
        }

        typewriterJob?.cancel()
        _uiState.update { it.copy(activeDialogue = null, dialogueLineIndex = 0, revealedChars = 0) }
        applyDialogueOutcome(event)
    }

    private fun applyDialogueOutcome(event: DialogueEvent) {
        val state = _uiState.value
        var ws = state.worldState
        var dirty = false

        event.rewardCompanion?.let { compId ->
            if (!ws.unlockedCompanions.contains(compId)) {
                ws = ws.copy(unlockedCompanions = ws.unlockedCompanions + compId)
                dirty = true
                val label = when (compId) {
                    CompanionIds.OWL -> "Moonlight Glide Unlocked! (Hold Jump)"
                    CompanionIds.FOX -> "Shadow Dash Unlocked! (Tap Dash)"
                    CompanionIds.MOTH -> "Flutter & Lantern Pulse Unlocked!"
                    else -> "Companion Blessing Unlocked!"
                }
                showAbilityBanner(label)
            }
        }

        event.rewardKitten?.let { kitId ->
            if (!ws.rescuedKittens.contains(kitId)) {
                ws = ws.copy(rescuedKittens = ws.rescuedKittens + kitId)
                dirty = true
            }
        }

        if (event.journalTitle != null && event.journalCategory != null) {
            val summary = event.lines.lastOrNull()?.text ?: ""
            val speaker = event.lines.firstOrNull()?.speakerName ?: "Forest"
            val ch = state.activeChapter?.number?.toLong() ?: 1L
            viewModelScope.launch {
                try {
                    repository.recordJournalEntry(
                        entryId = event.id,
                        chapter = ch,
                        category = event.journalCategory,
                        title = event.journalTitle,
                        speaker = speaker,
                        summary = summary
                    )
                } catch (_: Exception) {
                }
            }
        }

        if (event.triggersStoryFinale) {
            ws = ws.copy(
                storyCompleted = true,
                sequelUnlocked = true,
                highestUnlockedChapter = 5L
            )
            _uiState.update { it.copy(worldState = ws, screen = AppScreen.SEQUEL_EPILOGUE) }
            audioEngine.isFinaleMood = true
            persistWorldState(ws)
            return
        }

        if (event.triggersChapterComplete) {
            val curCh = state.activeChapter?.number ?: 1
            val nextCh = (curCh + 1).coerceAtMost(5)
            val highest = maxOf(ws.highestUnlockedChapter, nextCh.toLong())
            ws = ws.copy(
                currentChapter = nextCh.toLong(),
                highestUnlockedChapter = highest,
                lastCheckpointX = ChapterDefinitions.getChapter(nextCh).spawnX.toDouble()
            )
            _uiState.update { it.copy(worldState = ws) }
            persistWorldState(ws)
            startOrContinueChapter(nextCh, fromBeginningOfChapter = true)
            return
        }

        if (dirty) {
            _uiState.update { it.copy(worldState = ws) }
            persistWorldState(ws)
        }
    }

    fun stepSimulation(dtRaw: Float, input: PlayerInputState, viewportLogicalWidth: Float) {
        val state = _uiState.value
        val chapter = state.activeChapter ?: return
        if (state.screen != AppScreen.PLAYING || state.activeDialogue != null) return

        val dt = dtRaw.coerceIn(0.001f, 0.04f)
        simClock += dt

        val newTimeOfDay = (state.timeOfDay + dt * 0.009f) % 1.0f
        audioEngine.moodPhase = newTimeOfDay.toDouble()

        var ws = state.worldState
        val p = state.player

        var blinkTimer = p.blinkTimer - dt
        if (blinkTimer <= 0f) {
            blinkTimer = 2.6f + Random.nextFloat() * 2.2f
        }

        val hasOwl = ws.unlockedCompanions.contains(CompanionIds.OWL)
        val hasFox = ws.unlockedCompanions.contains(CompanionIds.FOX)
        val hasMoth = ws.unlockedCompanions.contains(CompanionIds.MOTH)

        var dashTimer = (p.dashTimer - dt).coerceAtLeast(0f)
        var dashCooldown = (p.dashCooldown - dt).coerceAtLeast(0f)
        var pulseTimer = (p.pulseTimer - dt).coerceAtLeast(0f)
        var pulseCooldown = (p.pulseCooldown - dt).coerceAtLeast(0f)
        val invulnTimer = (p.invulnTimer - dt).coerceAtLeast(0f)

        if (hasMoth && input.pulsePressedEdge && pulseCooldown <= 0f) {
            pulseTimer = 1.8f
            pulseCooldown = 3.2f
            audioEngine.playPulse()
            spawnBurst(p.x + 9f, p.y + 8f, 12, 0xFF8BE0B8L)
        }

        var facingRight = p.facingRight
        if (input.left && !input.right) facingRight = false
        if (input.right && !input.left) facingRight = true

        var vx = p.vx
        var vy = p.vy

        if (hasFox && input.dashPressedEdge && dashCooldown <= 0f && dashTimer <= 0f) {
            dashTimer = 0.19f
            dashCooldown = 0.85f
            vx = if (facingRight) 295f else -295f
            vy = 0f
            audioEngine.playDash()
            spawnBurst(p.x + 9f, p.y + 10f, 8, 0xFF64D8FFL)
        }

        if (dashTimer > 0f) {
            if (((simClock * 60f).toInt() % 3) == 0) {
                _dashGhosts.update { list -> (list + DashGhost(p.x, p.y, facingRight, 0.25f)).takeLast(6) }
            }
        } else {
            val moveDir = (if (input.right) 1f else 0f) - (if (input.left) 1f else 0f)
            val targetVx = moveDir * 124f
            val accel = if (p.onGround) 12f else 8f
            vx += (targetVx - vx) * (accel * dt).coerceAtMost(1f)
            if (abs(vx) < 2f && moveDir == 0f) vx = 0f
        }

        if (p.onGround) {
            coyoteTimer = 0.11f
        } else {
            coyoteTimer = (coyoteTimer - dt).coerceAtLeast(0f)
        }

        var hasDoubleJump = if (p.onGround && hasMoth) true else p.hasDoubleJump

        if (input.jumpPressedEdge) {
            if (coyoteTimer > 0f) {
                vy = -226f
                coyoteTimer = 0f
                audioEngine.playJump()
                spawnBurst(p.x + 9f, p.y + 15f, 5, 0xFFF6D37AL)
            } else if (hasMoth && hasDoubleJump) {
                vy = -205f
                hasDoubleJump = false
                audioEngine.playPulse()
                spawnBurst(p.x + 9f, p.y + 14f, 8, 0xFF8BE0B8L)
            }
        }

        var inWind = false
        for (h in chapter.hazards) {
            if (h.type == HazardType.WIND_GUST &&
                p.x + 14f > h.x && p.x + 4f < h.x + h.width &&
                p.y + 16f > h.y && p.y < h.y + h.height
            ) {
                inWind = true
                vy -= 360f * dt
            }
        }

        val isGliding = hasOwl && !p.onGround && vy > 0f && input.jumpHeld && dashTimer <= 0f
        if (isGliding && !p.isGliding) {
            audioEngine.playGlide()
        }

        if (dashTimer <= 0f) {
            val gravity = when {
                isGliding -> 150f
                inWind -> 290f
                else -> 560f
            }
            vy += gravity * dt
            val maxFall = if (isGliding) 42f else 280f
            if (vy > maxFall) vy = maxFall
        }

        var newX = (p.x + vx * dt).coerceIn(4f, chapter.worldWidth - 22f)
        val prevBottom = p.y + 16f
        var newY = p.y + vy * dt
        var newOnGround = false

        val catFootLeft = newX + 4f
        val catFootRight = newX + 14f

        if (vy >= 0f) {
            for (plat in chapter.platforms) {
                if (catFootRight > plat.x && catFootLeft < plat.x + plat.width) {
                    if (prevBottom <= plat.y + 6f && newY + 16f >= plat.y) {
                        newY = plat.y - 16f
                        if (!p.onGround && vy > 80f) {
                            audioEngine.playLand()
                        }
                        vy = 0f
                        newOnGround = true
                        break
                    }
                }
            }
        }

        stepAudioCooldown = (stepAudioCooldown - dt).coerceAtLeast(0f)
        if (newOnGround && abs(vx) > 35f && stepAudioCooldown <= 0f) {
            audioEngine.playPawstep()
            stepAudioCooldown = 0.24f
        }

        var hitHazard = false
        if (newY > 236f) {
            hitHazard = true
        } else if (invulnTimer <= 0f) {
            val catLeft = newX + 4f
            val catRight = newX + 14f
            val catTop = newY + 3f
            val catBottom = newY + 15f

            for (h in chapter.hazards) {
                when (h.type) {
                    HazardType.THORNS, HazardType.WATER -> {
                        if (catRight > h.x + 2f && catLeft < h.x + h.width - 2f &&
                            catBottom > h.y + 2f && catTop < h.y + h.height
                        ) {
                            hitHazard = true
                            break
                        }
                    }
                    HazardType.FALLING_SPORE -> {
                        if (pulseTimer <= 0f) {
                            val cycle = ((simClock * 0.75f + h.cycleOffset) % 2.4f)
                            val yOff = if (cycle < 1.2f) (cycle / 1.2f) * h.dropHeight else ((2.4f - cycle) / 1.2f) * h.dropHeight
                            val sy = h.y + yOff
                            if (catRight > h.x + 2f && catLeft < h.x + 14f &&
                                catBottom > sy + 2f && catTop < sy + 13f
                            ) {
                                hitHazard = true
                                break
                            }
                        }
                    }
                    HazardType.WIND_GUST -> Unit
                }
            }
        }

        if (hitHazard) {
            audioEngine.playHazardBump()
            spawnBurst(newX + 9f, newY + 8f, 10, 0xFFE27D8CL)
            val respawnX = ws.lastCheckpointX.toFloat().coerceIn(40f, chapter.worldWidth - 100f)
            val respawnY = findGroundYAt(chapter, respawnX) - 16f
            _uiState.update {
                it.copy(
                    player = p.copy(
                        x = respawnX,
                        y = respawnY,
                        vx = 0f,
                        vy = 0f,
                        onGround = true,
                        isGliding = false,
                        invulnTimer = 1.2f,
                        dashTimer = 0f
                    ),
                    timeOfDay = newTimeOfDay
                )
            }
            return
        }

        var worldDirty = false

        for (lantern in chapter.lanterns) {
            if (abs(newX + 9f - lantern.x) < 20f && abs(newY + 16f - lantern.y) < 26f) {
                if (!ws.activatedLanternIds.contains(lantern.id)) {
                    ws = ws.copy(
                        activatedLanternIds = ws.activatedLanternIds + lantern.id,
                        lastCheckpointX = lantern.x.toDouble(),
                        timeOfDay = newTimeOfDay.toDouble()
                    )
                    worldDirty = true
                    audioEngine.playLantern()
                    spawnBurst(lantern.x + 5f, lantern.y - 18f, 12, 0xFFF6D37AL)
                    lantern.loreLines?.let { lines ->
                        triggerDialogue(
                            DialogueEvent(
                                id = "lore_${lantern.id}",
                                lines = lines,
                                journalTitle = "Lantern in ${chapter.title}",
                                journalCategory = "lore"
                            )
                        )
                    }
                } else if (abs(ws.lastCheckpointX - lantern.x) > 40.0) {
                    ws = ws.copy(lastCheckpointX = lantern.x.toDouble())
                }
            }
        }

        for (ff in chapter.fireflies) {
            if (!ws.collectedFireflyIds.contains(ff.id)) {
                val dx = (newX + 9f) - ff.x
                val dy = (newY + 8f) - ff.y
                if (dx * dx + dy * dy < 260f) {
                    ws = ws.copy(
                        collectedFireflyIds = ws.collectedFireflyIds + ff.id,
                        totalFirefliesCollected = ws.totalFirefliesCollected + 1L
                    )
                    worldDirty = true
                    audioEngine.playFirefly()
                    spawnBurst(ff.x, ff.y, 10, if (chapter.isSequelPrologue) 0xFF64D8FFL else 0xFFF6D37AL)
                }
            }
        }

        chapter.companion?.let { npc ->
            if (!ws.unlockedCompanions.contains(npc.id) && abs(newX + 9f - npc.x) < 28f) {
                audioEngine.playLantern()
                triggerDialogue(
                    DialogueEvent(
                        id = "comp_${npc.id}",
                        lines = npc.dialogue,
                        rewardCompanion = npc.id,
                        journalTitle = "${npc.name} (${npc.abilityTitle})",
                        journalCategory = "companion"
                    )
                )
            }
        }

        chapter.kittenGoal?.let { kit ->
            if (abs(newX + 9f - kit.x) < 28f && abs(newY + 16f - kit.y) < 32f) {
                audioEngine.playKittenMew()
                spawnBurst(kit.x, kit.y - 8f, 18, kit.eyeColorHex)
                triggerDialogue(
                    DialogueEvent(
                        id = "kit_${kit.id}",
                        lines = kit.dialogue,
                        rewardKitten = kit.id,
                        triggersChapterComplete = !kit.isBlueFinale,
                        triggersStoryFinale = kit.isBlueFinale,
                        journalTitle = if (kit.isBlueFinale) "Reunited with Blue" else "Found ${kit.name}",
                        journalCategory = if (kit.isBlueFinale) "ending" else "kitten"
                    )
                )
            }
        }

        val targetBlueX = if (facingRight) newX - 24f else newX + 24f
        val newBlueX = p.blueCompanionX + (targetBlueX - p.blueCompanionX) * (4.5f * dt).coerceAtMost(1f)
        val blueGround = findGroundYAt(chapter, newBlueX) - 2f
        val newBlueY = p.blueCompanionY + (blueGround - p.blueCompanionY) * (7f * dt).coerceAtMost(1f)

        updateVisualEffects(dt)

        if (abs(newTimeOfDay - lastSavedTimeOfDay) > 0.10f) {
            ws = ws.copy(timeOfDay = newTimeOfDay.toDouble())
            lastSavedTimeOfDay = newTimeOfDay
            worldDirty = true
        }

        val targetCam = (newX - viewportLogicalWidth * 0.36f).coerceIn(0f, (chapter.worldWidth - viewportLogicalWidth).coerceAtLeast(0f))
        val smoothCam = state.cameraX + (targetCam - state.cameraX) * (7.5f * dt).coerceAtMost(1f)

        _uiState.update {
            it.copy(
                worldState = ws,
                timeOfDay = newTimeOfDay,
                cameraX = smoothCam,
                player = p.copy(
                    x = newX,
                    y = newY,
                    vx = vx,
                    vy = vy,
                    facingRight = facingRight,
                    onGround = newOnGround,
                    isGliding = isGliding,
                    hasDoubleJump = hasDoubleJump,
                    dashTimer = dashTimer,
                    dashCooldown = dashCooldown,
                    pulseTimer = pulseTimer,
                    pulseCooldown = pulseCooldown,
                    invulnTimer = invulnTimer,
                    animTime = simClock,
                    blinkTimer = blinkTimer,
                    blueCompanionX = newBlueX,
                    blueCompanionY = newBlueY
                )
            )
        }

        if (worldDirty) {
            persistWorldState(ws)
        }
    }

    private fun findGroundYAt(chapter: ChapterDefinition, x: Float): Float {
        return chapter.platforms
            .filter { x >= it.x - 4f && x <= it.x + it.width + 4f && !it.isBridge }
            .minByOrNull { it.y }?.y ?: 192f
    }

    private fun spawnBurst(x: Float, y: Float, count: Int, colorHex: Long) {
        val added = List(count) { i ->
            val angle = (i.toFloat() / count) * 6.28f + Random.nextFloat() * 0.5f
            val speed = 18f + Random.nextFloat() * 32f
            Particle(
                x = x,
                y = y,
                vx = kotlin.math.cos(angle) * speed,
                vy = sin(angle) * speed - 12f,
                life = 0.55f + Random.nextFloat() * 0.35f,
                maxLife = 0.9f,
                colorHex = colorHex,
                size = if (i % 2 == 0) 2.5f else 1.8f
            )
        }
        _particles.update { (it + added).takeLast(60) }
    }

    private fun updateVisualEffects(dt: Float) {
        if (_particles.value.isNotEmpty()) {
            _particles.update { list ->
                list.mapNotNull { pt ->
                    val nl = pt.life - dt
                    if (nl <= 0f) null
                    else pt.copy(
                        x = pt.x + pt.vx * dt,
                        y = pt.y + pt.vy * dt,
                        vy = pt.vy + 38f * dt,
                        life = nl
                    )
                }
            }
        }
        if (_dashGhosts.value.isNotEmpty()) {
            _dashGhosts.update { list ->
                list.mapNotNull { g ->
                    val nl = g.life - dt
                    if (nl <= 0f) null else g.copy(life = nl)
                }
            }
        }
    }

    private fun persistWorldState(ws: WorldState) {
        pendingSaveJob?.cancel()
        pendingSaveJob = viewModelScope.launch {
            try {
                repository.saveWorldState(ws)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Failed to save local world state", e)
            }
        }
    }

    fun getSimClock(): Float = simClock

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    private companion object {
        const val TAG = "GameViewModel"
    }
}
