package com.example.project2

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project2.ChatScreen.ChatScreen
import com.example.project2.ChatScreen.ChatViewModel
import com.example.project2.FrontPage.FrontScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.activity.viewModels
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.serialization.Serializable


@Composable
fun NavgationGraph(modifier: Modifier = Modifier,
                   navController: NavHostController = rememberNavController(),
                   startDestination : FrontScreenPage = FrontScreenPage,
                   chatViewModel : ChatViewModel
)
{
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = FrontScreenPage,
        modifier = modifier
    ){
        composable<FrontScreenPage>{
            FrontScreen(onClickJumpToAssistant = {navController.navigateSingleTopTo(ChatScreenPage)})
        }
        composable<ChatScreenPage> {
            ChatScreen(viewModel =  chatViewModel)
        }
    }
}


fun NavHostController.navigateSingleTopTo(route: Any) =
    this.navigate(route) {
        launchSingleTop = true
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id){ saveState = true }
        restoreState = true
    }