package com.example.project2.ChatScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.R
import com.example.project2.data.ChatDataTest
import com.example.project2.data.ChatItem

@Composable
fun ChatBubble(modifier: Modifier = Modifier, chatItems: ChatItem) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (chatItems.character == "bot") Arrangement.Start else Arrangement.End
    ) {
        if (chatItems.character == "bot") {
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
            )
        }

        Surface(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
            ,
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.padding(2.dp)
            ) {
                val bubblePainter = if (chatItems.character == "bot") {
                    painterResource(id = R.drawable.bgl2)
                } else {
                    painterResource(id = R.drawable.bg)
                }

                Image(
                    painter = bubblePainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    Text(
                        text = chatItems.chatText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(5.dp)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                }
            }
        }

        if (chatItems.character != "bot") {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
            )
        }
    }
}

@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    onClickToSend: (String) -> Unit = {},
    editableUserInputState: EditableUserInputState = rememberEditableUserInputState("")) {
    Surface (
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background
    ){
        Row (horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically){
            TextField(
                value = editableUserInputState.text,
                onValueChange = { editableUserInputState.updateText(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text("ask....")
                },
                modifier = modifier
                    .heightIn(min = 56.dp)
                    .widthIn(max = 300.dp)
                    .padding(2.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (editableUserInputState.isNotEmpty()) {
                        onClickToSend(editableUserInputState.text)
                        editableUserInputState.updateText("") // 清空输入框
                    }
                },
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send")
            }
        }
    }
}

@Composable
fun ChatFlow(modifier: Modifier = Modifier, chatItems: List<ChatItem>, isGenerating: Boolean){
    LazyColumn(
        modifier = modifier,
    ) {
        items(chatItems) { item ->
            ChatBubble(chatItems = item)
            Spacer(modifier = Modifier.padding(2.dp))
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 65.dp)
            ) {
                IndeterminateIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    isGenerating
                )
            }
        }
    }
    Spacer(Modifier.padding(bottom = 50.dp))
}

@Composable
fun rememberEditableUserInputState(hint: String) : EditableUserInputState =
    rememberSaveable (hint, saver = EditableUserInputState.Saver) {
        EditableUserInputState(hint, hint)
    }

class EditableUserInputState(private val hint: String, initialText : String)
{
    var text by mutableStateOf(initialText)
        private set

    fun updateText(newText : String){
        text = newText
    }

    fun isNotEmpty():Boolean{
        return text.isNotEmpty()
    }

    companion object {
        val Saver: Saver<EditableUserInputState, *> = listSaver(
            save = { listOf(it.hint, it.text) },
            restore = {
                EditableUserInputState(
                    hint = it[0],
                    initialText = it[1],
                )
            }
        )
    }
}




@Preview
@Composable
private fun ChatBalloonPrev() {
    val chatItems = ChatDataTest()
    val isGenerating = true
    ChatFlow(chatItems = chatItems.chatLists, isGenerating = isGenerating)
}

@Preview
@Composable
private fun InputPrev() {
    InputBar()
}