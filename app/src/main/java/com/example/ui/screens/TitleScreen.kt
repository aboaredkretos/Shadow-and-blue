package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.WorldState
import com.example.ui.theme.BlueKittenCyan
import com.example.ui.theme.DeepForestSlate
import com.example.ui.theme.FireflyGold
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.MistLavender
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SilhouetteBlack
import com.example.ui.theme.SoftSageGlow

@Composable
fun TitleScreen(
    worldState: WorldState,
    isMuted: Boolean,
    onContinueJourney: () -> Unit,
    onOpenChapters: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSequelEpilogue: () -> Unit,
    onToggleMute: () -> Unit
) {
    val hasStarted = worldState.activatedLanternIds.isNotEmpty() ||
        worldState.rescuedKittens.isNotEmpty() ||
        worldState.currentChapter > 1L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MidnightIndigo, DeepForestSlate, SilhouetteBlack)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = DeepForestSlate.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pets,
                            contentDescription = null,
                            tint = FireflyGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${worldState.rescuedKittens.size}/4 Kittens • ${worldState.totalFirefliesCollected} Fireflies",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentCream
                        )
                    }
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("title_mute_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = stringResource(R.string.hud_mute),
                        tint = ParchmentCream
                    )
                }
            }

            Card(
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.5.dp, FireflyGold.copy(alpha = 0.55f)),
                colors = CardDefaults.cardColors(containerColor = DeepForestSlate)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_forest_title_banner),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(MaterialTheme.shapes.large),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MidnightIndigo.copy(alpha = 0.1f),
                                        MidnightIndigo.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "SHADOW & BLUE",
                            style = MaterialTheme.typography.displayLarge,
                            color = ParchmentCream
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chapter ${worldState.currentChapter} • Saved on Device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueKittenCyan
                        )
                    }
                }
            }

            Button(
                onClick = onContinueJourney,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FireflyGold,
                    contentColor = SilhouetteBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("continue_journey_button")
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasStarted) stringResource(R.string.menu_continue) else stringResource(R.string.menu_begin),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenChapters,
                    border = BorderStroke(1.dp, MistLavender),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("chapters_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Forest,
                        contentDescription = null,
                        tint = SoftSageGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.menu_chapters),
                        style = MaterialTheme.typography.labelLarge,
                        color = ParchmentCream
                    )
                }

                OutlinedButton(
                    onClick = onOpenJournal,
                    border = BorderStroke(1.dp, MistLavender),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("journal_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = FireflyGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.menu_journal),
                        style = MaterialTheme.typography.labelLarge,
                        color = ParchmentCream
                    )
                }
            }

            if (worldState.sequelUnlocked) {
                Button(
                    onClick = onOpenSequelEpilogue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueKittenCyan.copy(alpha = 0.22f),
                        contentColor = BlueKittenCyan
                    ),
                    border = BorderStroke(1.5.dp, BlueKittenCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sequel_epilogue_button")
                ) {
                    Icon(imageVector = Icons.Rounded.Explore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.menu_sequel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (worldState.unlockedCompanions.isNotEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = DeepForestSlate.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Companion Blessings",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftSageGlow
                        )
                        Text(
                            text = worldState.unlockedCompanions.joinToString(" • ") {
                                when (it) {
                                    "owl" -> "Pip (Glide)"
                                    "fox" -> "Bramble (Dash)"
                                    "moth" -> "Luma (Flutter & Pulse)"
                                    else -> it
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentCream
                        )
                    }
                }
            }
        }
    }
}
