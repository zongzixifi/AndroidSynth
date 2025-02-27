package com.example.project2.ChatScreen

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ChatScreen(modifier: Modifier = Modifier, viewModel: ChatViewModel) {

    val chatItems by viewModel.chatItems
    val saveToSQL: (String, String) -> Unit = { character: String, chatText: String -> viewModel.addChatItem(character,chatText)}
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.onBackground)
            .padding(4.dp)) {
        ChatFlow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(bottom = 65.dp)
            ,chatItems = chatItems)
        InputBar(
            saveToSQL = saveToSQL,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp)
        )
    }
}

@Preview
@Composable
private fun ChatScreenPrev() {
//    val chatItems = ChatDataTest()
//    ChatScreen(chatItems = chatItems.chatLists)
}