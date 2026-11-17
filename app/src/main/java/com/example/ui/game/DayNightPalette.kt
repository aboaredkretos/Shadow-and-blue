package com.example.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class DayNightPalette(
    val skyTop: Color,
    val skyBottom: Color,
    val ridgeFar: Color,
    val treesFar: Color,
    val treesMid: Color,
    val groundSilhouette: Color,
    val mistColor: Color,
    val celestialColor: Color,
    val starAlpha: Float,
    val fireflyGlowAlpha: Float,
    val phaseLabel: String,
    val isNightOrDusk: Boolean
)

object DayNightCycle {
    private data class Keyframe(
        val t: Float,
        val skyTop: Color,
        val skyBottom: Color,
        val ridgeFar: Color,
        val treesFar: Color,
        val treesMid: Color,
        val ground: Color,
        val mist: Color,
        val celestial: Color,
        val starAlpha: Float,
        val fireflyAlpha: Float
    )

    private val keyframes = listOf(
        // 0.00: Deep Midnight
        Keyframe(
            t = 0.00f,
            skyTop = Color(0xFF0A0E1E),
            skyBottom = Color(0xFF1D2747),
            ridgeFar = Color(0xFF151C36),
            treesFar = Color(0xFF11162B),
            treesMid = Color(0xFF0B0F1E),
            ground = Color(0xFF05070E),
            mist = Color(0x5547618F),
            celestial = Color(0xFFF3F6FF),
            starAlpha = 0.95f,
            fireflyAlpha = 1.0f
        ),
        // 0.25: Misty Rose Dawn
        Keyframe(
            t = 0.25f,
            skyTop = Color(0xFF39345C),
            skyBottom = Color(0xFFD88E8D),
            ridgeFar = Color(0xFF544266),
            treesFar = Color(0xFF3B2F4D),
            treesMid = Color(0xFF231D33),
            ground = Color(0xFF0B0914),
            mist = Color(0x55EAB4A5),
            celestial = Color(0xFFFFD8A6),
            starAlpha = 0.25f,
            fireflyAlpha = 0.55f
        ),
        // 0.50: Soft Sage Golden Day
        Keyframe(
            t = 0.50f,
            skyTop = Color(0xFF47788A),
            skyBottom = Color(0xFFC9D9B8),
            ridgeFar = Color(0xFF5E8285),
            treesFar = Color(0xFF3D5C61),
            treesMid = Color(0xFF22373C),
            ground = Color(0xFF091217),
            mist = Color(0x45E9F2DA),
            celestial = Color(0xFFFFF2BD),
            starAlpha = 0.0f,
            fireflyAlpha = 0.30f
        ),
        // 0.75: Warm Apricot-Plum Dusk
        Keyframe(
            t = 0.75f,
            skyTop = Color(0xFF241E46),
            skyBottom = Color(0xFFC76E67),
            ridgeFar = Color(0xFF3F2C4F),
            treesFar = Color(0xFF2B1F3B),
            treesMid = Color(0xFF191329),
            ground = Color(0xFF080710),
            mist = Color(0x55C97B7C),
            celestial = Color(0xFFFDE2A7),
            starAlpha = 0.55f,
            fireflyAlpha = 0.85f
        ),
        // 1.00: Deep Midnight (wrap)
        Keyframe(
            t = 1.00f,
            skyTop = Color(0xFF0A0E1E),
            skyBottom = Color(0xFF1D2747),
            ridgeFar = Color(0xFF151C36),
            treesFar = Color(0xFF11162B),
            treesMid = Color(0xFF0B0F1E),
            ground = Color(0xFF05070E),
            mist = Color(0x5547618F),
            celestial = Color(0xFFF3F6FF),
            starAlpha = 0.95f,
            fireflyAlpha = 1.0f
        )
    )

    fun evaluate(rawTimeOfDay: Float): DayNightPalette {
        val t = ((rawTimeOfDay % 1f) + 1f) % 1f
        val idx = (0 until keyframes.size - 1).firstOrNull { t in keyframes[it].t..keyframes[it + 1].t } ?: 0
        val a = keyframes[idx]
        val b = keyframes[idx + 1]
        val frac = ((t - a.t) / (b.t - a.t)).coerceIn(0f, 1f)

        val label = when {
            t < 0.16f || t >= 0.86f -> "Midnight"
            t < 0.36f -> "Dawn"
            t < 0.64f -> "Daylight"
            else -> "Dusk"
        }
        return DayNightPalette(
            skyTop = lerp(a.skyTop, b.skyTop, frac),
            skyBottom = lerp(a.skyBottom, b.skyBottom, frac),
            ridgeFar = lerp(a.ridgeFar, b.ridgeFar, frac),
            treesFar = lerp(a.treesFar, b.treesFar, frac),
            treesMid = lerp(a.treesMid, b.treesMid, frac),
            groundSilhouette = lerp(a.ground, b.ground, frac),
            mistColor = lerp(a.mist, b.mist, frac),
            celestialColor = lerp(a.celestial, b.celestial, frac),
            starAlpha = a.starAlpha + (b.starAlpha - a.starAlpha) * frac,
            fireflyGlowAlpha = a.fireflyAlpha + (b.fireflyAlpha - a.fireflyAlpha) * frac,
            phaseLabel = label,
            isNightOrDusk = t < 0.28f || t > 0.68f
        )
    }

    fun celestialArcPosition(rawTimeOfDay: Float, width: Float, height: Float): Pair<Float, Float> {
        val t = ((rawTimeOfDay % 1f) + 1f) % 1f
        // Arc gently across the sky
        val angle = (t * 2f * PI).toFloat()
        val cx = width * (0.5f + 0.35f * sin(angle))
        val cy = height * (0.28f - 0.14f * cos(angle))
        return cx to cy
    }
}
