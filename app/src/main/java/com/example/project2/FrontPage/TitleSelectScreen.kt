package com.example.project2.FrontPage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.project2.FrontScreenPage
import com.example.project2.LoginPage.LoginViewModel
import com.example.project2.data.database.Session
import com.example.project2.navigateSingleTopTo
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.collectAsState
import com.example.project2.data.UserRepository
import com.example.project2.data.UserSessionManager

@Composable
fun TitleSelectScreen(
    onClickJumpFrontScreen: () -> Unit = {} ,
    sessionViewModel: TitleSelectViewModel = hiltViewModel(),
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    val sessions by sessionViewModel.sessionsByUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "对话历史",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp)
        ) {
            Text(text = "➕ 新建对话", color = Color(0xFF388E3C))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(sessions.filter { it.title.contains(searchQuery, ignoreCase = true) })
            { session ->
                SessionCard(session) {
                    sessionViewModel.selectSession(session)
                    onClickJumpFrontScreen()
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("新建对话") },
                text = {
                    TextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("请输入标题") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showError = false
                            UserSessionManager.userId?.let {
                                Log.d("insertSession", "当前用户：${UserSessionManager.userId}")
                                sessionViewModel.insertSession(
                                    title = newTitle,
                                    onSuccess = { onClickJumpFrontScreen() },
                                    onFailure = {text->
                                        errorText = text
                                        showError = true
                                    }
                                )
                            }
                            newTitle = ""
                            showDialog = false
                        },
                        //colors = TextButtonDefaults.textButtonColors(contentColor = Color(0xFF388E3C))
                    ) {
                        Text("创建")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        if (showError) {
            Text(
                text = errorText,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SessionCard(session: Session, onClick: () -> Unit) {
    val formattedTime = remember(session.lastUsedTime) {
        val date = Date(session.lastUsedTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.format(date)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Preview
@Composable
private fun TitleSelectScreenPrev() {
    TitleSelectScreen()
}