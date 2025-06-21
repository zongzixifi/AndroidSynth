package com.example.project2.ChatScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.project2.data.UserSessionManager
import com.example.project2.data.network.ChatIntent


@Composable
fun ChatScreen(modifier: Modifier = Modifier,
               viewModel: ChatViewModel = hiltViewModel(),
               onClickBack: () -> Unit,
               )
{
    val chatItems by viewModel.state.collectAsState()
    val selectedSessionId = remember { mutableStateOf(UserSessionManager.selectedSessionId) }

    LaunchedEffect(selectedSessionId.value) {
        selectedSessionId.value?.let {
            viewModel.handleIntent(ChatIntent.LoadSession(it))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(4.dp)) {
        Column(
                modifier = Modifier
                .align(Alignment.TopStart)
                .padding(bottom = 65.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(
                onClick = onClickBack,
                modifier = Modifier.padding(8.dp).align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
            ChatFlow(
                modifier = Modifier,
                chatItems = chatItems.chatItems,
                isGenerating = chatItems.isGenerating
            )
        }
        InputBar(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp),
            onClickToSend = { message->
                viewModel.handleIntent(intent = ChatIntent.SendMessage(message))
            }
        )
    }
}


@Composable
fun IndeterminateIndicator(modifier: Modifier = Modifier,isGenerating: Boolean) {
    if (!isGenerating) return

    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = Color.Blue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Generating...", fontSize = 14.sp, fontStyle = FontStyle.Italic)
    }
}

@Preview
@Composable
private fun ChatScreenPrev() {
//    val chatItems = ChatDataTest()
//   ChatScreen(chatItems = chatItems.chatLists)
}