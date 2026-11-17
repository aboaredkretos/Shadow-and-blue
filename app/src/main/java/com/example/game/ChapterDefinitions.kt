package com.example.game

import com.example.data.model.CompanionIds
import com.example.data.model.KittenIds

object ChapterDefinitions {

    // Logical canvas height = 240f. Ground top typically sits at y = 192f.
    fun getChapter(number: Int): ChapterDefinition = when (number) {
        1 -> chapter1
        2 -> chapter2
        3 -> chapter3
        4 -> chapter4
        5 -> chapter5Sequel
        else -> chapter1
    }

    val allChapters: List<ChapterDefinition> by lazy {
        listOf(chapter1, chapter2, chapter3, chapter4, chapter5Sequel)
    }

    private val chapter1 = ChapterDefinition(
        number = 1,
        title = "Whispering Timber",
        subtitle = "Garden Edge",
        hazardLabel = "Briar Thorns & Fallen Logs",
        worldWidth = 2580f,
        spawnX = 72f,
        spawnY = 160f,
        initialTimeHint = 0.78f,
        introDialogue = listOf(
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "The garden gate swung open in the twilight breeze... All four of my little ones wandered into the pines."
            ),
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "Hold still, little Blue, Soot, Pebble, and Clover. Mother is coming to bring you home."
            )
        ),
        platforms = listOf(
            PlatformRect(0f, 192f, 520f, 56f),
            PlatformRect(220f, 158f, 95f, 14f, isBridge = true),
            PlatformRect(460f, 168f, 120f, 16f),
            PlatformRect(580f, 192f, 440f, 56f),
            PlatformRect(780f, 148f, 110f, 14f, isBridge = true),
            PlatformRect(1100f, 182f, 240f, 64f),
            PlatformRect(1420f, 160f, 150f, 16f, isBridge = true),
            PlatformRect(1660f, 192f, 440f, 56f),
            PlatformRect(1840f, 152f, 100f, 14f, isBridge = true),
            PlatformRect(2170f, 186f, 410f, 60f)
        ),
        hazards = listOf(
            HazardZone("c1_h1", 315f, 180f, 56f, 12f, HazardType.THORNS),
            HazardZone("c1_h2", 990f, 212f, 125f, 34f, HazardType.WATER),
            HazardZone("c1_h3", 1340f, 212f, 320f, 34f, HazardType.WATER),
            HazardZone("c1_h4", 1740f, 180f, 65f, 12f, HazardType.THORNS),
            HazardZone("c1_h5", 2060f, 212f, 120f, 34f, HazardType.WATER)
        ),
        lanterns = listOf(
            LanternCheckpoint(
                "c1_l1",
                145f,
                192f,
                listOf(
                    DialogueLine(
                        "Forest Lantern",
                        SpeakerKind.NARRATOR,
                        "Warm lantern light anchors your steps in the forest's memory. Lit lanterns stay aglow whenever you return."
                    )
                )
            ),
            LanternCheckpoint("c1_l2", 890f, 192f),
            LanternCheckpoint("c1_l3", 1700f, 192f)
        ),
        fireflies = listOf(
            FireflyCollectible("c1_f1", 265f, 134f),
            FireflyCollectible("c1_f2", 515f, 140f),
            FireflyCollectible("c1_f3", 830f, 120f),
            FireflyCollectible("c1_f4", 1220f, 150f),
            FireflyCollectible("c1_f5", 1495f, 126f),
            FireflyCollectible("c1_f6", 1890f, 122f)
        ),
        companion = CompanionNpc(
            id = CompanionIds.OWL,
            name = "Pip the Barn Owl",
            speakerKind = SpeakerKind.OWL,
            abilityTitle = "Moonlight Glide",
            abilityDesc = "Hold Jump in mid-air to glide gently across wide hollows.",
            x = 650f,
            y = 192f,
            dialogue = listOf(
                DialogueLine(
                    "Pip",
                    SpeakerKind.OWL,
                    "Hoo-hoo! Easy there, midnight traveler. I saw four tiny shadows darting deeper under the canopy."
                ),
                DialogueLine(
                    "Pip",
                    SpeakerKind.OWL,
                    "Take one of my silver flight feathers! Hold Jump while airborne to catch the night wind and glide safely."
                )
            )
        ),
        kittenGoal = KittenGoal(
            id = KittenIds.SOOT,
            name = "Soot",
            speakerKind = SpeakerKind.SOOT,
            eyeColorHex = 0xFFF6D37AL,
            x = 2420f,
            y = 186f,
            dialogue = listOf(
                DialogueLine(
                    "Soot",
                    SpeakerKind.SOOT,
                    "Mew! Mama! I chased a golden moth and got tangled near the briar patch!"
                ),
                DialogueLine(
                    "Shadow",
                    SpeakerKind.SHADOW,
                    "Oh, my brave little Soot. Stay tucked safe inside my fur—now let's follow the brook for Pebble, Clover, and Blue."
                )
            )
        )
    )

    private val chapter2 = ChapterDefinition(
        number = 2,
        title = "Sunken Mire",
        subtitle = "Misty Brook",
        hazardLabel = "Deep Forest Streams & Mist",
        worldWidth = 2840f,
        spawnX = 72f,
        spawnY = 160f,
        initialTimeHint = 0.96f,
        introDialogue = listOf(
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "Mist hangs low over the sunken brook. One misstep into the cold water will sweep me back to the lantern bank."
            )
        ),
        platforms = listOf(
            PlatformRect(0f, 192f, 340f, 56f),
            PlatformRect(420f, 174f, 120f, 16f, isBridge = true),
            PlatformRect(600f, 192f, 360f, 56f),
            PlatformRect(1050f, 166f, 110f, 14f, isBridge = true),
            PlatformRect(1260f, 152f, 110f, 14f, isBridge = true),
            PlatformRect(1460f, 192f, 330f, 56f),
            PlatformRect(1880f, 172f, 110f, 14f, isBridge = true),
            PlatformRect(2120f, 156f, 120f, 14f, isBridge = true),
            PlatformRect(2340f, 192f, 500f, 56f)
        ),
        hazards = listOf(
            HazardZone("c2_h1", 340f, 206f, 260f, 40f, HazardType.WATER),
            HazardZone("c2_h2", 960f, 206f, 500f, 40f, HazardType.WATER),
            HazardZone("c2_h3", 1590f, 180f, 65f, 12f, HazardType.THORNS),
            HazardZone("c2_h4", 1790f, 206f, 550f, 40f, HazardType.WATER),
            HazardZone("c2_h5", 2460f, 180f, 55f, 12f, HazardType.THORNS)
        ),
        lanterns = listOf(
            LanternCheckpoint("c2_l1", 120f, 192f),
            LanternCheckpoint("c2_l2", 860f, 192f),
            LanternCheckpoint("c2_l3", 1520f, 192f),
            LanternCheckpoint("c2_l4", 2380f, 192f)
        ),
        fireflies = listOf(
            FireflyCollectible("c2_f1", 480f, 148f),
            FireflyCollectible("c2_f2", 1105f, 138f),
            FireflyCollectible("c2_f3", 1315f, 124f),
            FireflyCollectible("c2_f4", 1620f, 150f),
            FireflyCollectible("c2_f5", 1935f, 142f),
            FireflyCollectible("c2_f6", 2180f, 126f)
        ),
        companion = CompanionNpc(
            id = CompanionIds.FOX,
            name = "Bramble the Red Fox",
            speakerKind = SpeakerKind.FOX,
            abilityTitle = "Shadow Dash",
            abilityDesc = "Tap Dash to burst swiftly across wide brooks and hazards.",
            x = 700f,
            y = 192f,
            dialogue = listOf(
                DialogueLine(
                    "Bramble",
                    SpeakerKind.FOX,
                    "A cat out past midnight? You've got real grit! I spotted a green-eyed kit hopping across the far moss stones."
                ),
                DialogueLine(
                    "Bramble",
                    SpeakerKind.FOX,
                    "Let me teach you my quickstep—tap Dash to streak forward like a shadow across the wide water gaps!"
                )
            )
        ),
        kittenGoal = KittenGoal(
            id = KittenIds.PEBBLE,
            name = "Pebble",
            speakerKind = SpeakerKind.PEBBLE,
            eyeColorHex = 0xFF8BE0B8L,
            x = 2660f,
            y = 192f,
            dialogue = listOf(
                DialogueLine(
                    "Pebble",
                    SpeakerKind.PEBBLE,
                    "Mew-mew! Mama! The stepping stones wobbled, and I was too scared to cross back!"
                ),
                DialogueLine(
                    "Shadow",
                    SpeakerKind.SHADOW,
                    "You're safe now, sweet Pebble. Only Clover and little Blue remain—upward into the ancient canopy!"
                )
            )
        )
    )

    private val chapter3 = ChapterDefinition(
        number = 3,
        title = "Thornwood Canopy",
        subtitle = "Ancient Grove",
        hazardLabel = "Drifting Spores & High Boughs",
        worldWidth = 3000f,
        spawnX = 72f,
        spawnY = 160f,
        initialTimeHint = 0.24f,
        introDialogue = listOf(
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "First light is painting the treetops violet and rose. Watch out for falling dark spores from the high branches."
            )
        ),
        platforms = listOf(
            PlatformRect(0f, 192f, 440f, 56f),
            PlatformRect(260f, 152f, 100f, 14f, isBridge = true),
            PlatformRect(490f, 192f, 420f, 56f),
            PlatformRect(960f, 164f, 120f, 14f, isBridge = true),
            PlatformRect(1140f, 130f, 125f, 14f, isBridge = true),
            PlatformRect(1340f, 168f, 140f, 14f, isBridge = true),
            PlatformRect(1560f, 192f, 380f, 56f),
            PlatformRect(2020f, 156f, 115f, 14f, isBridge = true),
            PlatformRect(2210f, 122f, 125f, 14f, isBridge = true),
            PlatformRect(2430f, 188f, 570f, 60f)
        ),
        hazards = listOf(
            HazardZone("c3_h1", 310f, 180f, 55f, 12f, HazardType.THORNS),
            HazardZone("c3_s1", 560f, 92f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 0.0f, dropHeight = 88f),
            HazardZone("c3_h2", 910f, 208f, 650f, 38f, HazardType.WATER),
            HazardZone("c3_s2", 1200f, 58f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 1.4f, dropHeight = 76f),
            HazardZone("c3_s3", 1720f, 90f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 0.8f, dropHeight = 90f),
            HazardZone("c3_h3", 1940f, 208f, 490f, 38f, HazardType.WATER),
            HazardZone("c3_s4", 2560f, 88f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 2.0f, dropHeight = 88f)
        ),
        lanterns = listOf(
            LanternCheckpoint("c3_l1", 120f, 192f),
            LanternCheckpoint("c3_l2", 830f, 192f),
            LanternCheckpoint("c3_l3", 1620f, 192f),
            LanternCheckpoint("c3_l4", 2470f, 188f)
        ),
        fireflies = listOf(
            FireflyCollectible("c3_f1", 310f, 124f),
            FireflyCollectible("c3_f2", 1020f, 134f),
            FireflyCollectible("c3_f3", 1202f, 96f),
            FireflyCollectible("c3_f4", 1740f, 146f),
            FireflyCollectible("c3_f5", 2075f, 126f),
            FireflyCollectible("c3_f6", 2270f, 90f)
        ),
        companion = CompanionNpc(
            id = CompanionIds.MOTH,
            name = "Luma the Luna Moth",
            speakerKind = SpeakerKind.MOTH,
            abilityTitle = "Flutter & Lantern Pulse",
            abilityDesc = "Jump a second time in mid-air, and tap Pulse to calm nearby dark spores!",
            x = 690f,
            y = 192f,
            dialogue = listOf(
                DialogueLine(
                    "Luma",
                    SpeakerKind.MOTH,
                    "Shimmer softly, brave Shadow. High in the boughs, the morning mist hides thorny spores."
                ),
                DialogueLine(
                    "Luma",
                    SpeakerKind.MOTH,
                    "Share my wing-dust: tap Jump again mid-air for an extra flutter, and use Lantern Pulse to calm drifting spores!"
                )
            )
        ),
        kittenGoal = KittenGoal(
            id = KittenIds.CLOVER,
            name = "Clover",
            speakerKind = SpeakerKind.CLOVER,
            eyeColorHex = 0xFFFFB74DL,
            x = 2810f,
            y = 188f,
            dialogue = listOf(
                DialogueLine(
                    "Clover",
                    SpeakerKind.CLOVER,
                    "Mew! Mama, look! The glowing moth kept me warm under the ferns—but Blue kept running toward the old garden gate!"
                ),
                DialogueLine(
                    "Shadow",
                    SpeakerKind.SHADOW,
                    "Three kittens safe, and only little Blue left. Hold tight, everyone—we're heading to the Garden Wall!"
                )
            )
        )
    )

    private val chapter4 = ChapterDefinition(
        number = 4,
        title = "The Garden Gate",
        subtitle = "Moonlit Clearing",
        hazardLabel = "Midnight Gusts, Thorns & Spores",
        worldWidth = 3280f,
        spawnX = 72f,
        spawnY = 160f,
        initialTimeHint = 0.58f,
        introDialogue = listOf(
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "The air feels ancient here. Beyond this mossy clearing stands the old stone gate... Blue must be just ahead!"
            )
        ),
        platforms = listOf(
            PlatformRect(0f, 192f, 380f, 56f),
            PlatformRect(460f, 168f, 115f, 14f, isBridge = true),
            PlatformRect(680f, 142f, 115f, 14f, isBridge = true),
            PlatformRect(900f, 192f, 380f, 56f),
            PlatformRect(1380f, 162f, 120f, 14f, isBridge = true),
            PlatformRect(1600f, 134f, 120f, 14f, isBridge = true),
            PlatformRect(1820f, 174f, 290f, 74f),
            PlatformRect(2220f, 152f, 130f, 14f, isBridge = true),
            PlatformRect(2470f, 132f, 120f, 14f, isBridge = true),
            PlatformRect(2720f, 186f, 560f, 62f)
        ),
        hazards = listOf(
            HazardZone("c4_h1", 380f, 208f, 520f, 38f, HazardType.WATER),
            HazardZone("c4_w1", 560f, 90f, 110f, 95f, HazardType.WIND_GUST),
            HazardZone("c4_h2", 1040f, 180f, 65f, 12f, HazardType.THORNS),
            HazardZone("c4_s1", 1160f, 88f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 0.4f, dropHeight = 90f),
            HazardZone("c4_h3", 1280f, 208f, 540f, 38f, HazardType.WATER),
            HazardZone("c4_w2", 1480f, 82f, 115f, 95f, HazardType.WIND_GUST),
            HazardZone("c4_s2", 1950f, 76f, 24f, 24f, HazardType.FALLING_SPORE, cycleOffset = 1.2f, dropHeight = 86f),
            HazardZone("c4_h4", 2110f, 208f, 610f, 38f, HazardType.WATER),
            HazardZone("c4_w3", 2340f, 76f, 120f, 95f, HazardType.WIND_GUST)
        ),
        lanterns = listOf(
            LanternCheckpoint("c4_l1", 120f, 192f),
            LanternCheckpoint("c4_l2", 950f, 192f),
            LanternCheckpoint("c4_l3", 1870f, 174f),
            LanternCheckpoint("c4_l4", 2770f, 186f)
        ),
        fireflies = listOf(
            FireflyCollectible("c4_f1", 518f, 138f),
            FireflyCollectible("c4_f2", 738f, 112f),
            FireflyCollectible("c4_f3", 1440f, 132f),
            FireflyCollectible("c4_f4", 1660f, 102f),
            FireflyCollectible("c4_f5", 2285f, 120f),
            FireflyCollectible("c4_f6", 2530f, 100f)
        ),
        companion = null,
        kittenGoal = KittenGoal(
            id = KittenIds.BLUE,
            name = "Blue",
            speakerKind = SpeakerKind.BLUE,
            eyeColorHex = 0xFF64D8FFL,
            x = 3070f,
            y = 186f,
            isBlueFinale = true,
            dialogue = listOf(
                DialogueLine(
                    "Blue",
                    SpeakerKind.BLUE,
                    "Mew...! Mama! You found me! Look at the stars spilling over the stone wall..."
                ),
                DialogueLine(
                    "Shadow",
                    SpeakerKind.SHADOW,
                    "Oh, my little Blue... With your sky-bright eyes, I could spot you through any midnight mist."
                ),
                DialogueLine(
                    "Blue",
                    SpeakerKind.BLUE,
                    "Pip, Bramble, and Luma showed me the way to the gate. Everything out beyond the garden looks so vast and alive!"
                ),
                DialogueLine(
                    "Shadow",
                    SpeakerKind.SHADOW,
                    "Tonight, we rest together beneath our lanterns. Tomorrow, little Blue, that sprawling mysterious forest beyond the garden will be yours to explore."
                )
            )
        )
    )

    // Sequel Prologue unlocked after reuniting with Blue!
    private val chapter5Sequel = ChapterDefinition(
        number = 5,
        title = "Beyond the Garden",
        subtitle = "Sequel Prologue • Blue's New Life",
        hazardLabel = "Bioluminescent Grove (Peaceful)",
        worldWidth = 2200f,
        spawnX = 80f,
        spawnY = 160f,
        initialTimeHint = 0.88f,
        isSequelPrologue = true,
        introDialogue = listOf(
            DialogueLine(
                "Blue",
                SpeakerKind.BLUE,
                "Look, Mama! Beyond the old stone garden, the forest glows like a sea of blue stars! Wait for me!"
            ),
            DialogueLine(
                "Shadow",
                SpeakerKind.SHADOW,
                "Take your time, little Blue. Walk right beside me—let's see what wonders await in your new home."
            )
        ),
        platforms = listOf(
            PlatformRect(0f, 192f, 900f, 56f),
            PlatformRect(420f, 156f, 120f, 14f, isBridge = true),
            PlatformRect(940f, 184f, 440f, 62f),
            PlatformRect(1100f, 146f, 120f, 14f, isBridge = true),
            PlatformRect(1420f, 192f, 780f, 56f)
        ),
        hazards = emptyList(),
        lanterns = listOf(
            LanternCheckpoint(
                "c5_l1",
                500f,
                192f,
                listOf(
                    DialogueLine(
                        "Ancient Gatepost",
                        SpeakerKind.NARRATOR,
                        "Here ends the search through the garden woods, and here begins Blue's own tale among the luminous wilds."
                    )
                )
            ),
            LanternCheckpoint(
                "c5_l2",
                1200f,
                184f,
                listOf(
                    DialogueLine(
                        "Blue",
                        SpeakerKind.BLUE,
                        "Every firefly here hums like a tiny piano note! Can we come explore deeper tomorrow too?"
                    )
                )
            ),
            LanternCheckpoint(
                "c5_l3",
                1950f,
                192f,
                listOf(
                    DialogueLine(
                        "Shadow",
                        SpeakerKind.SHADOW,
                        "Wherever the forest stretches, little Blue, you'll always carry the lantern's warmth—and our story continues."
                    )
                )
            )
        ),
        fireflies = listOf(
            FireflyCollectible("c5_f1", 480f, 128f),
            FireflyCollectible("c5_f2", 820f, 156f),
            FireflyCollectible("c5_f3", 1160f, 118f),
            FireflyCollectible("c5_f4", 1560f, 156f),
            FireflyCollectible("c5_f5", 1780f, 148f)
        ),
        companion = null,
        kittenGoal = null
    )
}
