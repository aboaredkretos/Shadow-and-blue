package com.example.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.DialogueEvent
import com.example.game.SpeakerKind
import com.example.ui.theme.BlueKittenCyan
import com.example.ui.theme.DeepForestSlate
import com.example.ui.theme.FireflyGold
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.SoftSageGlow

@Composable
fun DialogueOverlay(
    event: DialogueEvent,
    lineIndex: Int,
    revealedChars: Int,
    onTapAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val line = event.lines.getOrNull(lineIndex) ?: return
    val visibleText = line.text.take(revealedChars.coerceAtMost(line.text.length))
    val isLineComplete = revealedChars >= line.text.length

    val accentColor = when (line.speakerKind) {
        SpeakerKind.BLUE -> BlueKittenCyan
        SpeakerKind.MOTH -> SoftSageGlow
        SpeakerKind.FOX -> Color(0xFFE8A45C)
        else -> FireflyGold
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, accentColor),
            colors = CardDefaults.cardColors(containerColor = MidnightIndigo.copy(alpha = 0.94f)),
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .clickable(onClick = onTapAdvance)
                .testTag("dialogue_box")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeakerPixelPortrait(
                    speakerKind = line.speakerKind,
                    accentColor = accentColor,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = line.speakerName,
                            style = MaterialTheme.typography.titleLarge,
                            color = accentColor
                        )
                        Text(
                            text = "${lineIndex + 1}/${event.lines.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = visibleText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ParchmentCream
                    )
                    if (isLineComplete) {
                        Text(
                            text = "${stringResource(R.string.dialogue_continue)} ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakerPixelPortrait(
    speakerKind: SpeakerKind,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val u = size.width / 16f
        fun p(gx: Float, gy: Float, gw: Float, gh: Float, c: Color) {
            drawRect(c, Offset(gx * u, gy * u), Size(gw * u, gh * u))
        }
        // Frame background
        p(0f, 0f, 16f, 16f, DeepForestSlate)

        when (speakerKind) {
            SpeakerKind.SHADOW -> {
                p(4f, 5f, 8f, 7f, Color(0xFF05060A))
                p(4f, 2f, 2.5f, 3f, Color(0xFF05060A))
                p(9.5f, 2f, 2.5f, 3f, Color(0xFF05060A))
                p(5.5f, 7.5f, 2f, 2f, Color.White)
                p(8.5f, 7.5f, 2f, 2f, Color.White)
            }
            SpeakerKind.BLUE, SpeakerKind.SOOT, SpeakerKind.PEBBLE, SpeakerKind.CLOVER -> {
                val eyeColor = when (speakerKind) {
                    SpeakerKind.BLUE -> BlueKittenCyan
                    SpeakerKind.SOOT -> FireflyGold
                    SpeakerKind.PEBBLE -> SoftSageGlow
                    else -> Color(0xFFFFB74D)
                }
                p(4.5f, 6f, 7f, 6f, Color(0xFF05060A))
                p(4.5f, 3.5f, 2f, 2.5f, Color(0xFF05060A))
                p(9.5f, 3.5f, 2f, 2.5f, Color(0xFF05060A))
                p(5.2f, 7.5f, 5.6f, 3.5f, eyeColor.copy(alpha = 0.28f))
                p(5.8f, 8.2f, 1.8f, 1.8f, eyeColor)
                p(8.6f, 8.2f, 1.8f, 1.8f, eyeColor)
            }
            SpeakerKind.OWL -> {
                p(4f, 4f, 8f, 9f, Color(0xFF0B0E17))
                p(3.5f, 2f, 3f, 2.5f, Color(0xFF0B0E17))
                p(9.5f, 2f, 3f, 2.5f, Color(0xFF0B0E17))
                p(5.5f, 6.5f, 2f, 2f, FireflyGold)
                p(8.5f, 6.5f, 2f, 2f, FireflyGold)
                p(7.5f, 8.5f, 1.5f, 2f, accentColor)
            }
            SpeakerKind.FOX -> {
                p(4f, 6f, 8f, 6f, Color(0xFF140D12))
                p(4f, 3f, 2.5f, 3f, Color(0xFF140D12))
                p(9.5f, 3f, 2.5f, 3f, Color(0xFF140D12))
                p(6f, 10f, 4f, 2.5f, Color(0xFFE8A45C))
                p(5.5f, 7.5f, 1.8f, 1.8f, Color.White)
                p(8.7f, 7.5f, 1.8f, 1.8f, Color.White)
            }
            SpeakerKind.MOTH -> {
                p(2f, 5f, 5f, 6f, SoftSageGlow.copy(alpha = 0.85f))
                p(9f, 5f, 5f, 6f, SoftSageGlow.copy(alpha = 0.85f))
                p(7f, 3f, 2f, 10f, Color(0xFF05060A))
                p(7.2f, 4.5f, 1.5f, 1.5f, Color.White)
            }
            SpeakerKind.NARRATOR -> {
                p(6.5f, 3f, 3f, 10f, Color(0xFF05060A))
                p(5f, 5f, 6f, 6f, FireflyGold.copy(alpha = 0.35f))
                p(6.8f, 6.5f, 2.4f, 3.5f, Color(0xFFFFF1C2))
            }
        }
    }
}
