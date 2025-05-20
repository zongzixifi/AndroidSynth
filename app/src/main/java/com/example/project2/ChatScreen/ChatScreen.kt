package com.example.project2.ChatScreen

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.MusicGenPage.MusicGenViewModel
import com.example.project2.data.ChatDataTest
import kotlinx.coroutines.flow.StateFlow


@Composable
fun ChatScreen(modifier: Modifier = Modifier, viewModel: ChatViewModel, onClickBack: () -> Unit) {

    val chatItems by viewModel.chatItems
    val isGenerating = viewModel.isGenerating
    val saveToSQL: (String, String) -> Unit = { character: String, chatText: String -> viewModel.addChatItem(character,chatText)}
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
                chatItems = chatItems
            )
            IndeterminateIndicator(modifier = Modifier.padding(bottom = 65.dp),isGenerating)
            }
        InputBar(
            saveToSQL = saveToSQL,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp)
        )
    }
}


@Composable
fun IndeterminateIndicator(modifier: Modifier = Modifier,isGenerating: StateFlow<Boolean>) {
    if (!isGenerating.collectAsState().value) return

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