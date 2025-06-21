package com.example.project2

import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project2.ChatScreen.ChatScreen
import com.example.project2.FrontPage.FrontScreen
import com.example.project2.FrontPage.TitleSelectScreen
import com.example.project2.LoginPage.LoginScreen
import com.example.project2.MusicGenPage.MusicGenerationScreen
import com.example.project2.SynthPage.DrumViewModel
import com.example.project2.SynthPage.MetronomeViewModel
import com.example.project2.SynthPage.SynthScreen
import java.io.File


@Composable
fun NavgationGraph(modifier: Modifier = Modifier,
                   navController: NavHostController = rememberNavController(),
                   startDestination : Any = LoginScreenPage,
                   metronomeViewModel: MetronomeViewModel,
                   drumViewModel:  DrumViewModel,
                   filepath: File,
                   context: Context
)

{
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable<FrontScreenPage>{
            FrontScreen(
                onClickJumpToAssistant = {navController.navigateSingleTopTo(ChatScreenPage)},
                onClickJumpToSynth = {navController.navigateSingleTopTo(SynthScreenPage)},
                onClickJumpToMusicGen = {navController.navigateSingleTopTo(MusicGenScreenPage)},
                onClickJumpToTitleSelect = {navController.navigateSingleTopTo(TitleSelectScreenPage)},
            )
        }

        composable<ChatScreenPage> {
            ChatScreen(
                onClickBack = {navController.navigateSingleTopTo(FrontScreenPage)},
                )
        }
        composable<SynthScreenPage> {
            SynthScreen(
                modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),
                metronomeViewModel = metronomeViewModel,
                filepath = filepath,
                drumViewModel = drumViewModel,
                onClickJumpFrontScreen = {navController.navigateSingleTopTo(FrontScreenPage)},
                )
        }
        composable<MusicGenScreenPage> {
            MusicGenerationScreen(
                context,
                modifier=Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),
                onClickJumpFrontScreen = {navController.navigateSingleTopTo(FrontScreenPage)},
                )
        }
        composable<LoginScreenPage> {
            LoginScreen(
                onClickJumpFrontScreen = {
                    navController.navigate(TitleSelectScreenPage) {
                        popUpTo<LoginScreenPage> { inclusive = true }
                    }},
            )
        }
        composable<TitleSelectScreenPage> {
            TitleSelectScreen(
                onClickJumpFrontScreen = {
                    navController.navigate(FrontScreenPage) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}


fun NavHostController.navigateSingleTopTo(route: Any) =
    this.navigate(route) {
        launchSingleTop = true
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id){ saveState = true }
        restoreState = true
    }
