package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.WorldState
import com.example.game.ChapterDefinition
import com.example.game.CompanionNpc
import com.example.game.DashGhost
import com.example.game.FireflyCollectible
import com.example.game.HazardType
import com.example.game.HazardZone
import com.example.game.KittenGoal
import com.example.game.LanternCheckpoint
import com.example.game.Particle
import com.example.game.PlatformRect
import com.example.game.PlayerRuntimeState
import com.example.game.SpeakerKind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

private const val LOGICAL_HEIGHT = 240f

@Composable
fun ForestCanvas(
    chapter: ChapterDefinition,
    worldState: WorldState,
    player: PlayerRuntimeState,
    cameraX: Float,
    timeOfDay: Float,
    animClock: Float,
    particles: List<Particle>,
    dashGhosts: List<DashGhost>,
    modifier: Modifier = Modifier
) {
    val palette = DayNightCycle.evaluate(timeOfDay)

    Canvas(modifier = modifier.fillMaxSize()) {
        val scale = size.height / LOGICAL_HEIGHT
        val viewLogicalW = size.width / scale

        // Helper to snap coordinates cleanly to the pixel grid
        fun px(lx: Float, ly: Float, lw: Float, lh: Float, color: Color) {
            drawRect(
                color = color,
                topLeft = Offset(floor(lx) * scale, floor(ly) * scale),
                size = Size(lw * scale + 0.6f, lh * scale + 0.6f)
            )
        }

        // 1. Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)),
            size = size
        )

        // 2. Twinkling Pixel Stars
        if (palette.starAlpha > 0.02f) {
            for (i in 0 until 38) {
                val sx = ((i * 97 + 23) % 420) * (viewLogicalW / 420f)
                val sy = ((i * 53 + 11) % 115).toFloat()
                val twinkle = 0.45f + 0.55f * sin(animClock * 2.2f + i * 0.9f)
                val alpha = (palette.starAlpha * twinkle).coerceIn(0f, 1f)
                val starSize = if (i % 5 == 0) 2f else 1.2f
                px(sx, sy, starSize, starSize, Color.White.copy(alpha = alpha))
            }
        }

        // 3. Pixel Moon / Sun with concentric halo
        val (celX, celY) = DayNightCycle.celestialArcPosition(timeOfDay, viewLogicalW, LOGICAL_HEIGHT)
        px(celX - 18f, celY - 18f, 36f, 36f, palette.celestialColor.copy(alpha = 0.10f))
        px(celX - 13f, celY - 13f, 26f, 26f, palette.celestialColor.copy(alpha = 0.22f))
        px(celX - 8f, celY - 8f, 16f, 16f, palette.celestialColor)
        if (palette.isNightOrDusk) {
            // Crescent cutout shadow
            px(celX - 3f, celY - 8f, 10f, 12f, palette.skyTop.copy(alpha = 0.85f))
        }

        // 4. Parallax Layer 1: Distant Mountain Ridges (0.08x)
        drawParallaxRidges(viewLogicalW, cameraX * 0.08f, palette.ridgeFar, ::px)

        // 5. Parallax Layer 2: Far Pine Forest Silhouettes (0.22x)
        drawParallaxTrees(viewLogicalW, cameraX * 0.22f, 198f, 52f, 36f, palette.treesFar, false, ::px)

        // 6. Parallax Layer 3: Midground Ancient Canopy & Hanging Vines (0.48x)
        drawParallaxTrees(viewLogicalW, cameraX * 0.48f, 212f, 84f, 54f, palette.treesMid, true, ::px)

        // 7. Parallax Layer 4: Drifting Mist & Background Fireflies (0.70x)
        for (m in 0 until 6) {
            val mistScreenX = (((m * 160f - cameraX * 0.70f + animClock * 8f) % (viewLogicalW + 180f)) + (viewLogicalW + 180f)) % (viewLogicalW + 180f) - 90f
            val mistY = 148f + (m % 3) * 16f
            px(mistScreenX, mistY, 88f, 8f, palette.mistColor)
            px(mistScreenX + 12f, mistY - 4f, 56f, 4f, palette.mistColor.copy(alpha = palette.mistColor.alpha * 0.6f))
        }
        for (bf in 0 until 12) {
            val fx = (((bf * 113f - cameraX * 0.68f + sin(animClock + bf) * 14f) % (viewLogicalW + 40f)) + (viewLogicalW + 40f)) % (viewLogicalW + 40f) - 20f
            val fy = 78f + ((bf * 37) % 90) + sin(animClock * 1.8f + bf) * 5f
            val glow = (0.35f + 0.55f * sin(animClock * 3.1f + bf * 1.3f)) * palette.fireflyGlowAlpha
            val fColor = if (chapter.isSequelPrologue) Color(0xFF64D8FF) else Color(0xFFF6D37A)
            px(fx - 1f, fy - 1f, 4f, 4f, fColor.copy(alpha = glow * 0.28f))
            px(fx, fy, 2f, 2f, fColor.copy(alpha = glow))
        }

        // World-space helpers (1.0x)
        // Sequel Gate Monument at end of Chapter 4 or start of Chapter 5
        if (chapter.number == 4 || chapter.isSequelPrologue) {
            val gateWorldX = if (chapter.number == 4) 3110f else 48f
            val gx = gateWorldX - cameraX
            if (gx > -80f && gx < viewLogicalW + 80f) {
                drawGardenStoneGate(gx, 186f, palette.groundSilhouette, chapter.isSequelPrologue, animClock, ::px)
            }
        }

        // 8. Hazards (Water, Thorns, Spores, Wind Gusts)
        chapter.hazards.forEach { hazard ->
            val hx = hazard.x - cameraX
            if (hx + hazard.width >= -20f && hx <= viewLogicalW + 20f) {
                drawHazard(hazard, hx, animClock, player.pulseTimer > 0f, palette, ::px)
            }
        }

        // 9. Platforms & Mossy Bridges
        chapter.platforms.forEach { plat ->
            val sx = plat.x - cameraX
            if (sx + plat.width >= -10f && sx <= viewLogicalW + 10f) {
                drawPlatform(plat, sx, palette.groundSilhouette, chapter.isSequelPrologue, ::px)
            }
        }

        // 10. Persistent Lantern Checkpoints
        chapter.lanterns.forEach { lantern ->
            val lx = lantern.x - cameraX
            if (lx >= -30f && lx <= viewLogicalW + 30f) {
                val isLit = worldState.activatedLanternIds.contains(lantern.id)
                drawLantern(lx, lantern.y, isLit, animClock, palette.groundSilhouette, ::px)
            }
        }

        // 11. Collectible Fireflies
        chapter.fireflies.forEach { firefly ->
            if (!worldState.collectedFireflyIds.contains(firefly.id)) {
                val fx = firefly.x - cameraX
                if (fx >= -20f && fx <= viewLogicalW + 20f) {
                    val bob = sin(animClock * 3.5f + firefly.x * 0.05f) * 3.5f
                    val fColor = if (chapter.isSequelPrologue) Color(0xFF64D8FF) else Color(0xFFF6D37A)
                    px(fx - 5f, firefly.y + bob - 5f, 10f, 10f, fColor.copy(alpha = 0.18f))
                    px(fx - 3f, firefly.y + bob - 3f, 6f, 6f, fColor.copy(alpha = 0.42f))
                    px(fx - 1.5f, firefly.y + bob - 1.5f, 3f, 3f, Color.White)
                }
            }
        }

        // 12. Animal Companion NPC
        chapter.companion?.let { npc ->
            val nx = npc.x - cameraX
            if (nx >= -40f && nx <= viewLogicalW + 40f) {
                val met = worldState.unlockedCompanions.contains(npc.id)
                drawCompanionNpc(npc, nx, animClock, met, palette.groundSilhouette, ::px)
            }
        }

        // 13. Lost Kitten Goal
        chapter.kittenGoal?.let { kitten ->
            val kx = kitten.x - cameraX
            if (kx >= -40f && kx <= viewLogicalW + 40f) {
                drawPixelKitten(
                    x = kx,
                    y = kitten.y,
                    eyeColor = Color(kitten.eyeColorHex),
                    animClock = animClock,
                    facingRight = false,
                    isMoving = false,
                    ::px
                )
            }
        }

        // 14. Dash Ghosts
        dashGhosts.forEach { ghost ->
            val gx = ghost.x - cameraX
            drawBlackCatSprite(
                x = gx,
                y = ghost.y,
                facingRight = ghost.facingRight,
                vx = 1f,
                vy = 0f,
                onGround = false,
                isGliding = false,
                isBlinking = false,
                animTime = player.animTime,
                silhouetteColor = Color(0xFF64D8FF).copy(alpha = ghost.life * 0.35f),
                eyeColor = Color.Transparent,
                px = ::px
            )
        }

        // 15. Blue following Shadow in Sequel Prologue (Chapter 5)!
        if (chapter.isSequelPrologue) {
            val bx = player.blueCompanionX - cameraX
            drawPixelKitten(
                x = bx,
                y = player.blueCompanionY,
                eyeColor = Color(0xFF64D8FF),
                animClock = animClock,
                facingRight = player.x >= player.blueCompanionX,
                isMoving = abs(player.x - player.blueCompanionX) > 22f,
                ::px
            )
        }

        // 16. Main Character: Shadow (Black Cat with White Eyes)
        val pxScreen = player.x - cameraX
        val blink = player.blinkTimer < 0.14f
        val catAlpha = if (player.invulnTimer > 0f && ((animClock * 18f).toInt() % 2 == 0)) 0.45f else 1f
        if (player.pulseTimer > 0f) {
            val pulseR = (1f - player.pulseTimer / 1.8f) * 68f
            drawCircle(
                color = Color(0xFF8BE0B8).copy(alpha = (player.pulseTimer / 1.8f) * 0.35f),
                radius = pulseR * scale,
                center = Offset((pxScreen + 9f) * scale, (player.y + 8f) * scale)
            )
        }
        drawBlackCatSprite(
            x = pxScreen,
            y = player.y,
            facingRight = player.facingRight,
            vx = player.vx,
            vy = player.vy,
            onGround = player.onGround,
            isGliding = player.isGliding,
            isBlinking = blink,
            animTime = player.animTime,
            silhouetteColor = Color(0xFF05060A).copy(alpha = catAlpha),
            eyeColor = Color.White.copy(alpha = catAlpha),
            px = ::px
        )

        // 17. Active Particles
        particles.forEach { pt ->
            val sx = pt.x - cameraX
            val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
            px(sx, pt.y, pt.size, pt.size, Color(pt.colorHex).copy(alpha = alpha))
        }

        // 18. Foreground Framing Silhouette Grass (1.25x)
        val fgSpacing = 44f
        val fgOffset = (cameraX * 1.25f) % fgSpacing
        var fgX = -fgOffset - 20f
        var idx = 0
        while (fgX < viewLogicalW + 20f) {
            val h = 8f + ((idx * 7) % 8)
            px(fgX, LOGICAL_HEIGHT - h, 3f, h, palette.groundSilhouette)
            px(fgX + 4f, LOGICAL_HEIGHT - h * 0.7f, 2f, h * 0.7f, palette.groundSilhouette)
            fgX += fgSpacing
            idx++
        }
    }
}

private inline fun drawParallaxRidges(
    viewW: Float,
    scrollX: Float,
    color: Color,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    val segW = 36f
    val startIdx = floor(scrollX / segW).toInt() - 1
    val endIdx = startIdx + (viewW / segW).toInt() + 4
    for (i in startIdx..endIdx) {
        val sx = i * segW - scrollX
        val peak = 112f + ((abs(i * 31 + 7) % 5) * 10f)
        px(sx, peak, segW + 1f, LOGICAL_HEIGHT - peak, color)
        px(sx + 6f, peak - 6f, segW - 12f, 6f, color)
    }
}

private inline fun drawParallaxTrees(
    viewW: Float,
    scrollX: Float,
    baseY: Float,
    spacing: Float,
    minHeight: Float,
    color: Color,
    withCanopyVines: Boolean,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    val startIdx = floor(scrollX / spacing).toInt() - 1
    val endIdx = startIdx + (viewW / spacing).toInt() + 3
    for (i in startIdx..endIdx) {
        val sx = i * spacing - scrollX
        val h = minHeight + ((abs(i * 47 + 13) % 5) * 14f)
        val trunkW = if (withCanopyVines) 8f else 5f
        val topY = baseY - h
        // Trunk
        px(sx + 10f, topY + 12f, trunkW, LOGICAL_HEIGHT - topY, color)
        // Pixel Pine Tiers
        px(sx + 2f, topY + 20f, trunkW + 16f, 10f, color)
        px(sx + 5f, topY + 11f, trunkW + 10f, 10f, color)
        px(sx + 8f, topY + 3f, trunkW + 4f, 9f, color)
        if (withCanopyVines && i % 2 == 0) {
            px(sx + 22f, 0f, 3f, 34f + (abs(i) % 3) * 10f, color)
            px(sx + 25f, 0f, 2f, 20f, color)
        }
    }
}

private inline fun drawPlatform(
    plat: PlatformRect,
    sx: Float,
    groundColor: Color,
    isSequelGrove: Boolean,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    if (plat.isBridge) {
        px(sx, plat.y, plat.width, 4f, groundColor)
        px(sx + 2f, plat.y + 4f, plat.width - 4f, plat.height - 4f, groundColor.copy(alpha = 0.85f))
        // Tiny mossy top edge pixels
        val mossColor = if (isSequelGrove) Color(0xFF2B6F84) else Color(0xFF1F3B36)
        var mx = 4f
        while (mx < plat.width - 6f) {
            px(sx + mx, plat.y - 1.5f, 6f, 2f, mossColor)
            mx += 16f
        }
    } else {
        px(sx, plat.y, plat.width, plat.height, groundColor)
        val grassTint = if (isSequelGrove) Color(0xFF286075) else Color(0xFF192B2B)
        var gx = 2f
        while (gx < plat.width - 4f) {
            val tuftH = if ((gx.toInt() % 3) == 0) 4f else 2.5f
            px(sx + gx, plat.y - tuftH, 2.5f, tuftH, grassTint)
            gx += 9f
        }
        // Glowing bioluminescent mushrooms in Chapter 5 Beyond the Garden!
        if (isSequelGrove) {
            var mx = 28f
            while (mx < plat.width - 28f) {
                px(sx + mx, plat.y - 5f, 2f, 5f, groundColor)
                px(sx + mx - 2f, plat.y - 8f, 6f, 3f, Color(0xFF64D8FF).copy(alpha = 0.85f))
                mx += 85f
            }
        }
    }
}

private inline fun drawHazard(
    hazard: HazardZone,
    hx: Float,
    animClock: Float,
    pulseActive: Boolean,
    palette: DayNightPalette,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    when (hazard.type) {
        HazardType.THORNS -> {
            var tx = 0f
            while (tx < hazard.width - 4f) {
                px(hx + tx, hazard.y + 6f, 6f, 6f, palette.groundSilhouette)
                px(hx + tx + 1f, hazard.y + 2f, 3f, 5f, Color(0xFF2A1422))
                px(hx + tx + 2f, hazard.y, 1.5f, 2.5f, Color(0xFFE27D8C).copy(alpha = 0.78f))
                tx += 7f
            }
        }
        HazardType.WATER -> {
            px(hx, hazard.y, hazard.width, hazard.height, Color(0xFF14273E).copy(alpha = 0.85f))
            val shimmerOffset = (animClock * 14f) % 16f
            var wx = shimmerOffset
            while (wx < hazard.width - 8f) {
                px(hx + wx, hazard.y + 2f, 7f, 1.5f, Color(0xFF64D8FF).copy(alpha = 0.45f))
                wx += 18f
            }
        }
        HazardType.FALLING_SPORE -> {
            val cycle = ((animClock * 0.75f + hazard.cycleOffset) % 2.4f)
            val yOffset = if (cycle < 1.2f) (cycle / 1.2f) * hazard.dropHeight else ((2.4f - cycle) / 1.2f) * hazard.dropHeight
            val sy = hazard.y + yOffset
            val calmedAlpha = if (pulseActive) 0.25f else 0.9f
            px(hx + 2f, sy + 2f, 12f, 12f, Color(0xFF1A0E1E).copy(alpha = calmedAlpha))
            px(hx, sy + 5f, 16f, 6f, Color(0xFF2D1533).copy(alpha = calmedAlpha))
            px(hx + 5f, sy + 5f, 5f, 5f, Color(0xFFE27D8C).copy(alpha = calmedAlpha * 0.8f))
        }
        HazardType.WIND_GUST -> {
            for (w in 0 until 4) {
                val wy = hazard.y + hazard.height - ((animClock * 42f + w * 24f) % hazard.height)
                val wx = hx + 12f + (w * 24f) + sin(animClock * 4f + w) * 5f
                px(wx, wy, 2f, 10f, Color(0xFFA9E4FF).copy(alpha = 0.42f))
            }
        }
    }
}

private inline fun drawLantern(
    lx: Float,
    groundY: Float,
    isLit: Boolean,
    animClock: Float,
    silhouetteColor: Color,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    // Wooden post & hanging hook
    px(lx - 1.5f, groundY - 26f, 3f, 26f, silhouetteColor)
    px(lx - 1.5f, groundY - 26f, 9f, 2.5f, silhouetteColor)
    px(lx + 5.5f, groundY - 24f, 1.5f, 4f, silhouetteColor)
    // Lantern cage
    px(lx + 3f, groundY - 20f, 6f, 7f, silhouetteColor)
    if (isLit) {
        val pulse = 0.78f + 0.22f * sin(animClock * 4.5f)
        px(lx - 3f, groundY - 25f, 18f, 18f, Color(0xFFF6D37A).copy(alpha = 0.16f * pulse))
        px(lx + 1f, groundY - 21f, 10f, 10f, Color(0xFFE8A45C).copy(alpha = 0.35f * pulse))
        px(lx + 4.5f, groundY - 18.5f, 3f, 4f, Color(0xFFFFF1C2))
    } else {
        px(lx + 4.5f, groundY - 18.5f, 3f, 4f, Color(0xFF474E75).copy(alpha = 0.45f))
    }
}

private inline fun drawCompanionNpc(
    npc: CompanionNpc,
    nx: Float,
    animClock: Float,
    alreadyMet: Boolean,
    silhouetteColor: Color,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    val bob = sin(animClock * 2.5f) * 2f
    when (npc.speakerKind) {
        SpeakerKind.OWL -> {
            val oy = npc.y - 18f + bob * 0.4f
            // Barn owl silhouette
            px(nx - 6f, oy, 12f, 15f, silhouetteColor)
            px(nx - 7f, oy - 3f, 4f, 4f, silhouetteColor)
            px(nx + 3f, oy - 3f, 4f, 4f, silhouetteColor)
            // Pale glowing eyes
            px(nx - 3.5f, oy + 4f, 2.5f, 2.5f, Color(0xFFF6D37A))
            px(nx + 1f, oy + 4f, 2.5f, 2.5f, Color(0xFFF6D37A))
        }
        SpeakerKind.FOX -> {
            val fy = npc.y - 14f
            // Fox body + bushy tail
            px(nx - 8f, fy + 4f, 14f, 8f, silhouetteColor)
            px(nx + 2f, fy - 2f, 8f, 8f, silhouetteColor)
            px(nx + 3f, fy - 5f, 2.5f, 3.5f, silhouetteColor)
            px(nx + 7f, fy - 5f, 2.5f, 3.5f, silhouetteColor)
            // Bushy tail with glowing warm tip
            px(nx - 15f, fy + 2f, 8f, 5f, silhouetteColor)
            px(nx - 17f, fy + 2f, 3f, 4f, Color(0xFFE8A45C))
            px(nx + 6f, fy, 2f, 2f, Color.White)
        }
        SpeakerKind.MOTH -> {
            val my = npc.y - 26f + bob * 1.4f
            val wingSpan = 6f + abs(sin(animClock * 6f)) * 6f
            px(nx - wingSpan, my - 4f, wingSpan, 9f, Color(0xFF8BE0B8).copy(alpha = 0.78f))
            px(nx + 2f, my - 4f, wingSpan, 9f, Color(0xFF8BE0B8).copy(alpha = 0.78f))
            px(nx - 1f, my - 5f, 3f, 11f, silhouetteColor)
            px(nx - 0.5f, my - 3f, 2f, 2f, Color.White)
        }
        else -> {}
    }
    if (!alreadyMet) {
        px(nx - 2f, npc.y - 34f + bob, 4f, 4f, Color(0xFFF6D37A).copy(alpha = 0.85f))
    }
}

private inline fun drawPixelKitten(
    x: Float,
    y: Float,
    eyeColor: Color,
    animClock: Float,
    facingRight: Boolean,
    isMoving: Boolean,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    val hop = if (isMoving) abs(sin(animClock * 10f)) * 3.5f else abs(sin(animClock * 2.2f)) * 1.2f
    val ky = y - 10f - hop
    val silhouette = Color(0xFF06070C)
    // Tiny cute body
    px(x - 5f, ky + 4f, 10f, 5f, silhouette)
    // Tiny legs
    px(x - 4f, ky + 9f, 2f, 2f, silhouette)
    px(x + 2f, ky + 9f, 2f, 2f, silhouette)
    // Tiny head + ears
    val headX = if (facingRight) x + 1f else x - 6f
    px(headX, ky, 6f, 5f, silhouette)
    px(headX, ky - 2f, 2f, 2f, silhouette)
    px(headX + 4f, ky - 2f, 2f, 2f, silhouette)
    // Curled tail
    val tailX = if (facingRight) x - 8f else x + 5f
    px(tailX, ky + 1f, 3f, 4f, silhouette)
    // Luminous signature eyes (Blue's eyes glow vivid sky-blue!)
    px(headX - 1f, ky + 1f, 7f, 4f, eyeColor.copy(alpha = 0.25f))
    px(headX + 1f, ky + 2f, 1.8f, 1.8f, eyeColor)
    px(headX + 3.8f, ky + 2f, 1.8f, 1.8f, eyeColor)
}

private inline fun drawBlackCatSprite(
    x: Float,
    y: Float,
    facingRight: Boolean,
    vx: Float,
    vy: Float,
    onGround: Boolean,
    isGliding: Boolean,
    isBlinking: Boolean,
    animTime: Float,
    silhouetteColor: Color,
    eyeColor: Color,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    // Owl feather aura when gliding
    if (isGliding) {
        px(x - 2f, y - 5f, 22f, 8f, Color(0xFFF6D37A).copy(alpha = 0.35f))
    }

    val dir = if (facingRight) 1f else -1f
    val bodyX = x + 3f
    val bodyY = y + 5f

    // Sleek cat body
    px(bodyX, bodyY, 12f, 7f, silhouetteColor)

    // Animated Tail
    val tailWag = sin(animTime * if (abs(vx) > 5f) 11f else 3f) * 2.5f
    val tailBaseX = if (facingRight) bodyX - 4f else bodyX + 12f
    px(tailBaseX, bodyY + 1f + tailWag * 0.4f, 5f, 2.5f, silhouetteColor)
    px(tailBaseX - dir * 3f, bodyY - 3f + tailWag, 3.5f, 4f, silhouetteColor)

    // Head & Pointed Ears
    val headX = if (facingRight) bodyX + 7f else bodyX - 3f
    val headY = y + (if (!onGround && vy < 0f) -1f else 0f)
    px(headX, headY, 8f, 7f, silhouetteColor)
    px(headX + 0.5f, headY - 3f, 2.5f, 3f, silhouetteColor)
    px(headX + 5f, headY - 3f, 2.5f, 3f, silhouetteColor)

    // White Eyes (blink or alert)
    if (!isBlinking && eyeColor != Color.Transparent) {
        val eyeOffsetX = if (facingRight) 2.5f else 1f
        px(headX + eyeOffsetX, headY + 2.5f, 2f, 2f, eyeColor)
        px(headX + eyeOffsetX + 3f, headY + 2.5f, 2f, 2f, eyeColor)
    }

    // Articulated 4-frame running / airborne legs
    val stride = if (onGround && abs(vx) > 10f) sin(animTime * 16f) * 3f else 0f
    val legY = bodyY + 7f
    if (!onGround) {
        px(bodyX + 1f, legY - 1f, 2.5f, 4f, silhouetteColor)
        px(bodyX + 9f, legY - 2f, 2.5f, 4f, silhouetteColor)
    } else {
        px(bodyX + 1f + stride, legY, 2.5f, 4f, silhouetteColor)
        px(bodyX + 4f - stride, legY, 2.5f, 4f, silhouetteColor)
        px(bodyX + 8f - stride, legY, 2.5f, 4f, silhouetteColor)
        px(bodyX + 10.5f + stride, legY, 2.5f, 4f, silhouetteColor)
    }
}

private inline fun drawGardenStoneGate(
    gx: Float,
    groundY: Float,
    stoneColor: Color,
    isSequel: Boolean,
    animClock: Float,
    px: (Float, Float, Float, Float, Color) -> Unit
) {
    // Ancient Stone Garden Archway
    px(gx, groundY - 56f, 10f, 56f, stoneColor)
    px(gx + 46f, groundY - 56f, 10f, 56f, stoneColor)
    px(gx - 4f, groundY - 64f, 64f, 10f, stoneColor)
    px(gx + 6f, groundY - 72f, 44f, 8f, stoneColor)
    // Shimmering portal mist inside the open gate
    val glowAlpha = 0.22f + 0.12f * sin(animClock * 2.4f)
    val portalColor = if (isSequel) Color(0xFF64D8FF) else Color(0xFF8BE0B8)
    px(gx + 10f, groundY - 54f, 36f, 54f, portalColor.copy(alpha = glowAlpha))
}
