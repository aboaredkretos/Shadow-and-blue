package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class ForestAudioEngine {

    private data class SynthVoice(
        val freq: Double,
        val durationSec: Double,
        val volume: Double,
        val waveform: Waveform,
        var elapsedSec: Double = 0.0,
        val freqEnd: Double = freq
    )

    private enum class Waveform {
        LOFI_PIANO,
        CHIME,
        MEW,
        NOISE_WHOOSH,
        THUD
    }

    private val sampleRate = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var audioJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private val activeVoices = ConcurrentLinkedQueue<SynthVoice>()

    @Volatile
    var isMuted: Boolean = false
        private set

    @Volatile
    var isPlayingMusic: Boolean = true

    @Volatile
    var moodPhase: Double = 0.78 // 0..1 day-night cycle

    @Volatile
    var isFinaleMood: Boolean = false

    private var lowPassPrev = 0.0
    private var breezeFilter = 0.0

    // Cozy Lo-Fi Piano chord progressions (MIDI note numbers)
    private val duskNightChords = listOf(
        listOf(57, 64, 67, 71, 72), // Am9
        listOf(53, 60, 64, 69, 72), // Fmaj7
        listOf(48, 55, 60, 64, 71), // Cmaj7
        listOf(55, 62, 67, 71, 74)  // G6
    )

    private val dawnDayChords = listOf(
        listOf(53, 60, 64, 69, 72), // Fmaj7
        listOf(52, 59, 62, 67, 71), // Em7
        listOf(50, 57, 60, 65, 69), // Dm7
        listOf(48, 55, 60, 64, 71)  // Cmaj7
    )

    private val finaleChords = listOf(
        listOf(48, 55, 64, 67, 74), // Cmaj9
        listOf(55, 59, 62, 67, 74), // G/B
        listOf(57, 64, 67, 72, 76), // Am7
        listOf(53, 60, 65, 69, 72)  // Fmaj7
    )

    private val melodyNotes = listOf(76, 79, 81, 79, 74, 72, 74, 76, 83, 81, 79, 74)

    fun start() {
        if (audioJob?.isActive == true) return
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) {
            return
        }

        audioTrack = track
        try {
            track.play()
        } catch (_: Exception) {
            return
        }

        audioJob = scope.launch {
            val chunkSamples = 512
            val buffer = ShortArray(chunkSamples)
            val dt = 1.0 / sampleRate
            var musicClock = 0.0
            var nextBeatSec = 0.1
            var beatStep = 0
            val stepSec = 0.44 // ~68 BPM eighth-notes

            while (isActive) {
                if (isMuted) {
                    buffer.fill(0)
                    try {
                        track.write(buffer, 0, buffer.size)
                    } catch (_: Exception) {
                        break
                    }
                    continue
                }

                for (i in 0 until chunkSamples) {
                    if (isPlayingMusic && musicClock >= nextBeatSec) {
                        triggerMusicStep(beatStep)
                        beatStep++
                        nextBeatSec += stepSec
                    }

                    var mix = 0.0
                    val iterator = activeVoices.iterator()
                    while (iterator.hasNext()) {
                        val voice = iterator.next()
                        if (voice.elapsedSec >= voice.durationSec) {
                            iterator.remove()
                            continue
                        }
                        val t = voice.elapsedSec
                        val progress = t / voice.durationSec
                        val curFreq = voice.freq + (voice.freqEnd - voice.freq) * progress
                        val sample = when (voice.waveform) {
                            Waveform.LOFI_PIANO -> {
                                val env = (1.0 - exp(-t * 48.0)) * exp(-t * 2.1)
                                val fundamental = sin(2.0 * PI * curFreq * t)
                                val second = 0.28 * sin(2.0 * PI * curFreq * 2.0 * t) * exp(-t * 4.0)
                                val third = 0.08 * sin(2.0 * PI * curFreq * 3.0 * t) * exp(-t * 6.5)
                                (fundamental + second + third) * env * voice.volume
                            }
                            Waveform.CHIME -> {
                                val env = (1.0 - exp(-t * 90.0)) * exp(-t * 5.2)
                                val s = sin(2.0 * PI * curFreq * t) + 0.35 * sin(2.0 * PI * curFreq * 2.5 * t)
                                s * env * voice.volume
                            }
                            Waveform.MEW -> {
                                val env = sin(PI * progress).coerceAtLeast(0.0)
                                val s = sin(2.0 * PI * curFreq * t) + 0.3 * sin(2.0 * PI * curFreq * 2.0 * t)
                                s * env * voice.volume
                            }
                            Waveform.NOISE_WHOOSH -> {
                                val env = sin(PI * progress).coerceAtLeast(0.0)
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                white * env * voice.volume
                            }
                            Waveform.THUD -> {
                                val env = exp(-t * 18.0)
                                sin(2.0 * PI * curFreq * t) * env * voice.volume
                            }
                        }
                        mix += sample
                        voice.elapsedSec += dt
                    }

                    // Subtle cozy forest night breeze & lo-fi warmth
                    val rawNoise = Random.nextDouble() * 2.0 - 1.0
                    breezeFilter = breezeFilter * 0.985 + rawNoise * 0.015
                    mix += breezeFilter * 0.035

                    // Gentle warm low-pass rolloff for lo-fi felt piano character
                    lowPassPrev = lowPassPrev * 0.58 + mix * 0.42
                    val clipped = (lowPassPrev * 0.55).coerceIn(-0.95, 0.95)
                    buffer[i] = (clipped * Short.MAX_VALUE).toInt().toShort()
                    musicClock += dt
                }

                try {
                    track.write(buffer, 0, buffer.size)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    private fun triggerMusicStep(step: Int) {
        if (activeVoices.size > 24) return
        val progression = when {
            isFinaleMood -> finaleChords
            moodPhase in 0.22..0.68 -> dawnDayChords
            else -> duskNightChords
        }
        val chordIdx = (step / 8) % progression.size
        val chord = progression[chordIdx]
        val stepInBar = step % 8

        when (stepInBar) {
            0 -> {
                // Soft felt chord roll
                chord.take(4).forEachIndexed { idx, midi ->
                    val vol = if (idx == 0) 0.16 else 0.10
                    enqueueVoice(
                        SynthVoice(
                            freq = midiToFreq(midi),
                            durationSec = 2.8,
                            volume = vol,
                            waveform = Waveform.LOFI_PIANO,
                            elapsedSec = -idx * 0.03
                        )
                    )
                }
            }
            3 -> {
                // Gentle arpeggiated top note
                val midi = chord[3]
                enqueueVoice(SynthVoice(midiToFreq(midi), 1.6, 0.09, Waveform.LOFI_PIANO))
            }
            5 -> {
                // Cozy sparse lo-fi melody note
                val mNote = melodyNotes[(step / 4 + chordIdx) % melodyNotes.size]
                enqueueVoice(SynthVoice(midiToFreq(mNote), 1.8, 0.11, Waveform.LOFI_PIANO))
            }
            7 -> {
                if (Random.nextFloat() < 0.6f) {
                    val mNote = chord.last() + 12
                    enqueueVoice(SynthVoice(midiToFreq(mNote), 1.4, 0.07, Waveform.LOFI_PIANO))
                }
            }
        }
    }

    private fun enqueueVoice(voice: SynthVoice) {
        if (isMuted) return
        if (activeVoices.size < 32) {
            activeVoices.add(voice.copy(elapsedSec = voice.elapsedSec.coerceAtLeast(0.0)))
        }
    }

    private fun midiToFreq(midi: Int): Double = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) activeVoices.clear()
        return isMuted
    }

    fun playJump() {
        enqueueVoice(SynthVoice(freq = 330.0, freqEnd = 520.0, durationSec = 0.14, volume = 0.14, waveform = Waveform.CHIME))
    }

    fun playLand() {
        enqueueVoice(SynthVoice(freq = 120.0, freqEnd = 65.0, durationSec = 0.09, volume = 0.15, waveform = Waveform.THUD))
    }

    fun playPawstep() {
        enqueueVoice(SynthVoice(freq = 160.0, freqEnd = 100.0, durationSec = 0.04, volume = 0.045, waveform = Waveform.NOISE_WHOOSH))
    }

    fun playGlide() {
        enqueueVoice(SynthVoice(freq = 260.0, freqEnd = 220.0, durationSec = 0.22, volume = 0.08, waveform = Waveform.NOISE_WHOOSH))
    }

    fun playDash() {
        enqueueVoice(SynthVoice(freq = 480.0, freqEnd = 190.0, durationSec = 0.18, volume = 0.14, waveform = Waveform.NOISE_WHOOSH))
        enqueueVoice(SynthVoice(freq = 587.0, freqEnd = 440.0, durationSec = 0.16, volume = 0.10, waveform = Waveform.CHIME))
    }

    fun playPulse() {
        enqueueVoice(SynthVoice(freq = 659.25, durationSec = 0.35, volume = 0.14, waveform = Waveform.CHIME))
        enqueueVoice(SynthVoice(freq = 880.0, durationSec = 0.45, volume = 0.12, waveform = Waveform.CHIME))
    }

    fun playFirefly() {
        enqueueVoice(SynthVoice(freq = 987.77, freqEnd = 1318.5, durationSec = 0.28, volume = 0.13, waveform = Waveform.CHIME))
    }

    fun playLantern() {
        enqueueVoice(SynthVoice(freq = 523.25, durationSec = 0.65, volume = 0.15, waveform = Waveform.CHIME))
        enqueueVoice(SynthVoice(freq = 783.99, durationSec = 0.75, volume = 0.12, waveform = Waveform.CHIME))
    }

    fun playDialogueBlip() {
        enqueueVoice(SynthVoice(freq = 440.0 + Random.nextInt(-30, 50), durationSec = 0.055, volume = 0.06, waveform = Waveform.LOFI_PIANO))
    }

    fun playKittenMew() {
        enqueueVoice(SynthVoice(freq = 740.0, freqEnd = 960.0, durationSec = 0.18, volume = 0.17, waveform = Waveform.MEW))
        enqueueVoice(SynthVoice(freq = 523.25, durationSec = 1.2, volume = 0.14, waveform = Waveform.LOFI_PIANO))
        enqueueVoice(SynthVoice(freq = 659.25, durationSec = 1.4, volume = 0.12, waveform = Waveform.LOFI_PIANO))
        enqueueVoice(SynthVoice(freq = 783.99, durationSec = 1.6, volume = 0.12, waveform = Waveform.LOFI_PIANO))
    }

    fun playHazardBump() {
        enqueueVoice(SynthVoice(freq = 180.0, freqEnd = 95.0, durationSec = 0.18, volume = 0.16, waveform = Waveform.THUD))
    }

    fun release() {
        audioJob?.cancel()
        audioJob = null
        activeVoices.clear()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        scope.cancel()
    }
}
