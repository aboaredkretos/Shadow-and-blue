package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CompanionIds
import com.example.game.DashGhost
import com.example.game.GameSessionUiState
import com.example.game.Particle
import com.example.game.PlayerInputState
import com.example.ui.game.DayNightCycle
import com.example.ui.game.DialogueOverlay
import com.example.ui.game.ForestCanvas
import com.example.ui.theme.BlueKittenCyan
import com.example.ui.theme.DeepForestSlate
import com.example.ui.theme.FireflyGold
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.MistLavender
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SilhouetteBlack
import com.example.ui.theme.SoftSageGlow

@Composable
fun GameplayScreen(
    uiState: GameSessionUiState,
    particles: List<Particle>,
    dashGhosts: List<DashGhost>,
    onStepSimulation: (Float, PlayerInputState, Float) -> Unit,
    onAdvanceDialogue: () -> Unit,
    onToggleMute: () -> Unit,
    onRestartChapter: () -> Unit,
    onExitToMenu: () -> Unit,
    getSimClock: () -> Float
) {
    val chapter = uiState.activeChapter ?: return

    var isPaused by remember { mutableStateOf(false) }
    var leftHeld by remember { mutableStateOf(false) }
    var rightHeld by remember { mutableStateOf(false) }
    var jumpHeld by remember { mutableStateOf(false) }
    var jumpEdge by remember { mutableStateOf(false) }
    var dashEdge by remember { mutableStateOf(false) }
    var pulseEdge by remember { mutableStateOf(false) }

    BackHandler {
        isPaused = !isPaused
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val logicalWidth = if (maxHeight.value > 0f) {
            240f * (maxWidth.value / maxHeight.value)
        } else {
            420f
        }

        LaunchedEffect(chapter.number, isPaused, uiState.activeDialogue != null) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    if (lastNanos != 0L && !isPaused && uiState.activeDialogue == null) {
                        val dt = (nanos - lastNanos) / 1_000_000_000f
                        onStepSimulation(
                            dt,
                            PlayerInputState(
                                left = leftHeld,
                                right = rightHeld,
                                jumpHeld = jumpHeld,
                                jumpPressedEdge = jumpEdge,
                                dashPressedEdge = dashEdge,
                                pulsePressedEdge = pulseEdge
                            ),
                            logicalWidth
                        )
                        jumpEdge = false
                        dashEdge = false
                        pulseEdge = false
                    }
                    lastNanos = nanos
                }
            }
        }

        // 1. Real-time Multi-layered Pixel Silhouette Canvas
        ForestCanvas(
            chapter = chapter,
            worldState = uiState.worldState,
            player = uiState.player,
            cameraX = uiState.cameraX,
            timeOfDay = uiState.timeOfDay,
            animClock = getSimClock(),
            particles = particles,
            dashGhosts = dashGhosts,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Cozy HUD (respecting status bar & notch safeDrawing)
        val palette = DayNightCycle.evaluate(uiState.timeOfDay)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MidnightIndigo.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (palette.isNightOrDusk) Icons.Rounded.NightsStay else Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = FireflyGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${palette.phaseLabel} • Ch.${chapter.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentCream
                    )
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = BlueKittenCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${uiState.worldState.rescuedKittens.size}/4",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueKittenCyan
                    )
                    Icon(
                        imageVector = Icons.Rounded.Flare,
                        contentDescription = null,
                        tint = FireflyGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${uiState.worldState.totalFirefliesCollected}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FireflyGold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hud_mute_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MidnightIndigo.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (uiState.isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = stringResource(R.string.hud_mute),
                            tint = ParchmentCream,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { isPaused = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hud_pause_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MidnightIndigo.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = stringResource(R.string.hud_pause),
                            tint = ParchmentCream,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // 3. Chapter / Ability Toast Banners
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimatedVisibility(visible = uiState.chapterBannerTitle != null, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MidnightIndigo.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, FireflyGold.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = uiState.chapterBannerTitle.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = ParchmentCream,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            AnimatedVisibility(visible = uiState.abilityBanner != null, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = DeepForestSlate.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, SoftSageGlow)
                ) {
                    Text(
                        text = uiState.abilityBanner.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftSageGlow,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 4. Dialogue Box OR Touch Controls
        if (uiState.activeDialogue != null) {
            DialogueOverlay(
                event = uiState.activeDialogue,
                lineIndex = uiState.dialogueLineIndex,
                revealedChars = uiState.revealedChars,
                onTapAdvance = onAdvanceDialogue,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        } else if (!isPaused) {
            val hasFox = uiState.worldState.unlockedCompanions.contains(CompanionIds.FOX)
            val hasMoth = uiState.worldState.unlockedCompanions.contains(CompanionIds.MOTH)
            val hasOwl = uiState.worldState.unlockedCompanions.contains(CompanionIds.OWL)

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // D-pad Left / Right
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HoldControlButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDesc = stringResource(R.string.controls_left),
                        accent = ParchmentCream,
                        isPressed = leftHeld,
                        onPressChange = { pressed -> leftHeld = pressed },
                        modifier = Modifier.testTag("control_left")
                    )
                    HoldControlButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDesc = stringResource(R.string.controls_right),
                        accent = ParchmentCream,
                        isPressed = rightHeld,
                        onPressChange = { pressed -> rightHeld = pressed },
                        modifier = Modifier.testTag("control_right")
                    )
                }

                // Abilities: Pulse (Moth), Dash (Fox), Jump / Glide (Owl)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (hasMoth) {
                        val ready = uiState.player.pulseCooldown <= 0f
                        HoldControlButton(
                            icon = Icons.Rounded.Flare,
                            contentDesc = stringResource(R.string.controls_pulse),
                            accent = if (ready) SoftSageGlow else MistLavender,
                            isPressed = false,
                            onPressChange = { pressed ->
                                if (pressed) pulseEdge = true
                            },
                            sizeDp = 54,
                            modifier = Modifier.testTag("control_pulse")
                        )
                    }
                    if (hasFox) {
                        val ready = uiState.player.dashCooldown <= 0f
                        HoldControlButton(
                            icon = Icons.Rounded.Bolt,
                            contentDesc = stringResource(R.string.controls_dash),
                            accent = if (ready) BlueKittenCyan else MistLavender,
                            isPressed = false,
                            onPressChange = { pressed ->
                                if (pressed) dashEdge = true
                            },
                            sizeDp = 54,
                            modifier = Modifier.testTag("control_dash")
                        )
                    }
                    HoldControlButton(
                        icon = Icons.Rounded.ArrowUpward,
                        contentDesc = stringResource(R.string.controls_jump),
                        accent = if (hasOwl && uiState.player.isGliding) FireflyGold else FireflyGold,
                        isPressed = jumpHeld,
                        onPressChange = { pressed ->
                            if (pressed && !jumpHeld) jumpEdge = true
                            jumpHeld = pressed
                        },
                        sizeDp = 64,
                        modifier = Modifier.testTag("control_jump")
                    )
                }
            }
        }

        // 5. Pause Overlay
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SilhouetteBlack.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.5.dp, FireflyGold),
                    colors = CardDefaults.cardColors(containerColor = DeepForestSlate),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.86f)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ParchmentCream
                        )
                        Text(
                            text = "Chapter ${chapter.number}: ${chapter.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueKittenCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { isPaused = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FireflyGold,
                                contentColor = SilhouetteBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("pause_resume_button")
                        ) {
                            Text("Resume", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = {
                                isPaused = false
                                onRestartChapter()
                            },
                            border = BorderStroke(1.dp, MistLavender),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pause_restart_button")
                        ) {
                            Text("Restart Chapter", style = MaterialTheme.typography.labelLarge, color = ParchmentCream)
                        }
                        OutlinedButton(
                            onClick = {
                                isPaused = false
                                onExitToMenu()
                            },
                            border = BorderStroke(1.dp, MistLavender),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pause_menu_button")
                        ) {
                            Text("Sanctuary Menu", style = MaterialTheme.typography.labelLarge, color = ParchmentCream)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HoldControlButton(
    icon: ImageVector,
    contentDesc: String,
    accent: Color,
    isPressed: Boolean,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 58
) {
    Surface(
        shape = CircleShape,
        color = if (isPressed) accent.copy(alpha = 0.28f) else MidnightIndigo.copy(alpha = 0.72f),
        border = BorderStroke(1.5.dp, accent.copy(alpha = if (isPressed) 1f else 0.7f)),
        modifier = modifier
            .size(sizeDp.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressChange(true)
                    waitForUpOrCancellation()
                    onPressChange(false)
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = accent,
                modifier = Modifier.size((sizeDp * 0.48f).dp)
            )
        }
    }
}
