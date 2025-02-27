package com.example.project2.FrontPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FrontScreen(modifier: Modifier = Modifier, onClickJumpToAssistant: () -> Unit ={}) {
    val ClassContainersSetterChatPage = ClassContainerSetter(
        text = "Don'n know how to get start?\n" +
                "\n" +
                "Ask your AI assistant!",
        icon = Icons.Filled.Face,
        color = MaterialTheme.colorScheme.primary
    )

    val ClassContainersSetterMusicPage = ClassContainerSetter(
        text = "start with demo music",
        icon = Icons.Filled.Create,
        color = MaterialTheme.colorScheme.secondaryContainer
    )
    val ClassContainersSetterGeneratePage = ClassContainerSetter(
        text = "start with only text...",
        icon = Icons.Filled.Edit,
        color = MaterialTheme.colorScheme.tertiaryContainer
    )

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.tertiary
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(Modifier.padding(vertical = 5.dp))
            ModelContainer(setter =  ClassContainersSetterChatPage, onClick = onClickJumpToAssistant)
            Spacer(Modifier.padding(vertical = 5.dp))
            ModelContainer(setter =  ClassContainersSetterMusicPage)
            Spacer(Modifier.padding(vertical = 5.dp))
            ModelContainer(setter =  ClassContainersSetterGeneratePage)
            Spacer(Modifier.padding(vertical = 5.dp))
        }
    }
}

@Preview
@Composable
private fun FrontScreenPrev() {
    FrontScreen()
}
