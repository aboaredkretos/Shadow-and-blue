package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.WorldStateRepository
import com.example.game.AppScreen
import com.example.game.GameViewModel
import com.example.ui.screens.ChapterSelectScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SequelEpilogueScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.ShadowBlueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShadowBlueTheme {
                ShadowBlueApp()
            }
        }
    }
}

@Composable
fun ShadowBlueApp() {
    val gameViewModel: GameViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                GameViewModel(repository = WorldStateRepository(app))
            }
        }
    )

    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val journalEntries by gameViewModel.journalEntries.collectAsStateWithLifecycle()
    val particles by gameViewModel.particles.collectAsStateWithLifecycle()
    val dashGhosts by gameViewModel.dashGhosts.collectAsStateWithLifecycle()

    when (uiState.screen) {
        AppScreen.TITLE -> {
            TitleScreen(
                worldState = uiState.worldState,
                isMuted = uiState.isMuted,
                onContinueJourney = { gameViewModel.startOrContinueChapter() },
                onOpenChapters = { gameViewModel.navigateTo(AppScreen.CHAPTERS) },
                onOpenJournal = { gameViewModel.navigateTo(AppScreen.JOURNAL) },
                onOpenSequelEpilogue = { gameViewModel.navigateTo(AppScreen.SEQUEL_EPILOGUE) },
                onToggleMute = { gameViewModel.toggleMute() }
            )
        }
        AppScreen.CHAPTERS -> {
            ChapterSelectScreen(
                worldState = uiState.worldState,
                onSelectChapter = { chNum ->
                    gameViewModel.startOrContinueChapter(chNum, fromBeginningOfChapter = true)
                },
                onBack = { gameViewModel.navigateTo(AppScreen.TITLE) }
            )
        }
        AppScreen.JOURNAL -> {
            JournalScreen(
                entries = journalEntries,
                onBack = { gameViewModel.navigateTo(AppScreen.TITLE) }
            )
        }
        AppScreen.SEQUEL_EPILOGUE -> {
            SequelEpilogueScreen(
                worldState = uiState.worldState,
                onPlaySequelPrologue = {
                    gameViewModel.startOrContinueChapter(5, fromBeginningOfChapter = true)
                },
                onReturnToTitle = { gameViewModel.navigateTo(AppScreen.TITLE) }
            )
        }
        AppScreen.PLAYING -> {
            GameplayScreen(
                uiState = uiState,
                particles = particles,
                dashGhosts = dashGhosts,
                onStepSimulation = { dt, input, viewW ->
                    gameViewModel.stepSimulation(dt, input, viewW)
                },
                onAdvanceDialogue = { gameViewModel.advanceOrSkipDialogue() },
                onToggleMute = { gameViewModel.toggleMute() },
                onRestartChapter = {
                    val ch = uiState.activeChapter?.number ?: 1
                    gameViewModel.startOrContinueChapter(ch, fromBeginningOfChapter = true)
                },
                onExitToMenu = { gameViewModel.navigateTo(AppScreen.TITLE) },
                getSimClock = { gameViewModel.getSimClock() }
            )
        }
    }
}
