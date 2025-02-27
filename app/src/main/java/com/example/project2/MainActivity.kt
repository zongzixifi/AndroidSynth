package com.example.project2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.example.project2.ChatScreen.ChatScreen
import com.example.project2.ChatScreen.ChatViewModel
import com.example.project2.data.DBHelper
import com.example.project2.ui.theme.Project2Theme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp


class MainActivity : ComponentActivity() {
    private val chatViewModel : ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    ChatScreen(
//                        modifier = Modifier.padding(innerPadding),
//                        chatViewModel
//                    )
                    NavgationGraph(
                        modifier = Modifier.padding(innerPadding),
                        chatViewModel = chatViewModel
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2Theme {
//        ChatScreen(
//            modifier = Modifier.fillMaxSize()
//        )
    }
}