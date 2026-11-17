package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun SequelEpilogueScreen(
    worldState: WorldState,
    onPlaySequelPrologue: () -> Unit,
    onReturnToTitle: () -> Unit
) {
    BackHandler(onBack = onReturnToTitle)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MidnightIndigo, DeepForestSlate, SilhouetteBlack)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.5.dp, BlueKittenCyan.copy(alpha = 0.8f)),
                colors = CardDefaults.cardColors(containerColor = DeepForestSlate)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_sequel_beyond_garden),
                        contentDescription = "Shadow and Blue at the stone gate beyond the garden",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.large),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MidnightIndigo.copy(alpha = 0.05f),
                                        MidnightIndigo.copy(alpha = 0.82f)
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
                            text = "ALL KITTENS HOME",
                            style = MaterialTheme.typography.labelSmall,
                            color = FireflyGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beyond the Garden",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BlueKittenCyan
                        )
                    }
                }
            }

            Card(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MistLavender.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = DeepForestSlate.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reunited beneath the moonlit gate, Shadow gathers Soot, Pebble, Clover, and little blue-eyed Blue.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ParchmentCream,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "As dawn breaks past the mossy wall, Blue gazes out into a sprawling, luminous forest—ready for a brand-new life and a grand sequel beyond the garden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${worldState.totalFirefliesCollected} Fireflies Gathered • ${worldState.activatedLanternIds.size} Lanterns Lit",
                        style = MaterialTheme.typography.labelSmall,
                        color = FireflyGold
                    )
                }
            }

            Button(
                onClick = onPlaySequelPrologue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueKittenCyan,
                    contentColor = SilhouetteBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_sequel_prologue_button")
            ) {
                Icon(imageVector = Icons.Rounded.Explore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore With Blue (Sequel Prologue)",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onReturnToTitle,
                border = BorderStroke(1.dp, MistLavender),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("epilogue_home_button")
            ) {
                Text(
                    text = "Return to Sanctuary Menu",
                    style = MaterialTheme.typography.labelLarge,
                    color = ParchmentCream
                )
            }
        }
    }
}
