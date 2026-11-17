package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.WorldState
import com.example.game.ChapterDefinitions
import com.example.ui.theme.BlueKittenCyan
import com.example.ui.theme.DeepForestSlate
import com.example.ui.theme.FireflyGold
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.MistLavender
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SilhouetteBlack
import com.example.ui.theme.SoftSageGlow

@Composable
fun ChapterSelectScreen(
    worldState: WorldState,
    onSelectChapter: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MidnightIndigo, DeepForestSlate, SilhouetteBlack)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chapters_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = ParchmentCream
                    )
                }
                Text(
                    text = stringResource(R.string.menu_chapters),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentCream
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ChapterDefinitions.allChapters, key = { it.number }) { chapter ->
                    val unlocked = chapter.number <= worldState.highestUnlockedChapter.toInt()
                    val kittenFound = chapter.kittenGoal?.let { worldState.rescuedKittens.contains(it.id) } ?: false
                    val accent = when {
                        chapter.isSequelPrologue -> BlueKittenCyan
                        kittenFound -> SoftSageGlow
                        unlocked -> FireflyGold
                        else -> MistLavender
                    }

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.2.dp, accent.copy(alpha = if (unlocked) 0.7f else 0.3f)),
                        colors = CardDefaults.cardColors(
                            containerColor = DeepForestSlate.copy(alpha = if (unlocked) 0.92f else 0.48f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (unlocked) {
                                    Modifier.clickable { onSelectChapter(chapter.number) }
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("chapter_card_${chapter.number}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (chapter.isSequelPrologue) {
                                        "Sequel Prologue • ${chapter.title}"
                                    } else {
                                        "Chapter ${chapter.number} • ${chapter.title}"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (unlocked) ParchmentCream else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${chapter.subtitle} — ${chapter.hazardLabel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                chapter.kittenGoal?.let { kit ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (kittenFound) "Reunited with ${kit.name}" else "Lost Kitten: ${kit.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent
                                    )
                                }
                            }

                            Icon(
                                imageVector = when {
                                    !unlocked -> Icons.Rounded.Lock
                                    kittenFound -> Icons.Rounded.CheckCircle
                                    else -> Icons.Rounded.PlayArrow
                                },
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
