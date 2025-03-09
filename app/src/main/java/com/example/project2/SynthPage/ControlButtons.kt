package com.example.project2.SynthPage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.FluidSynthManager.destroyFluidSynthLoop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun ToggleButton(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onStop: () -> Unit,
    textStart: String,
    textend: String,
    ColorOnStart: Color = MaterialTheme.colorScheme.primary,
    ColorOnEnd: Color = MaterialTheme.colorScheme.secondary,
    shape: Shape = RectangleShape
) {
    var isTriggered by remember { mutableStateOf(false) }

    Button(
        shape = shape,
        onClick = {
            isTriggered = !isTriggered
            if (isTriggered) {
                onStart()
            } else {
                onStop()
            }
        },
        modifier = modifier
            .padding(1.dp)
        ,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) ColorOnStart else ColorOnEnd
        )
    ) {
        Text(text = if (isTriggered) textStart else textend)
    }
}

@Composable
fun ToggleButtonIcon(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onStop: () -> Unit,
    IconStart: ImageVector,
    Iconend: ImageVector,
    ColorOnStart: Color = MaterialTheme.colorScheme.primary,
    ColorOnEnd: Color = MaterialTheme.colorScheme.secondary,
    shape: Shape = RectangleShape
) {
    var isTriggered by remember { mutableStateOf(false) }

    Button(
        shape = shape,
        onClick = {
            isTriggered = !isTriggered
            if (isTriggered) {
                onStart()
            } else {
                onStop()
            }
        },
        modifier = modifier
            .padding(1.dp)
        ,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) ColorOnStart else ColorOnEnd
        )
    ) {
        Icon(
            imageVector = if (isTriggered) IconStart else Iconend,
            contentDescription = ""
        )
    }
}

@Composable
fun Buttons(modifier: Modifier = Modifier, filepath: File) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleButton(
                modifier = Modifier.padding(2.dp),
                onStart = {
                    FluidSynthManager.startRecording()
                },
                onStop = { FluidSynthManager.stopRecording() },
                textStart = "rec",
                textend = "rec",
                ColorOnStart = Color.Red,
                ColorOnEnd = Color.Gray,
            )
            ToggleButtonIcon(
                modifier = Modifier.padding(2.dp),
                onStart = {
                    FluidSynthManager.startPlayback()
                },
                onStop = { FluidSynthManager.stopPlayback() },
                IconStart = Icons.Filled.Pause,
                Iconend = Icons.Outlined.PlayArrow
            )
            Button(
                shape = RectangleShape,
                modifier = Modifier.padding(2.dp),
                onClick = { FluidSynthManager.clearLoop() }) {
                Text(text = "Clear")
            }
            ToggleButtonIcon(
                modifier = Modifier.padding(2.dp),
                onStart = {
                    FluidSynthManager.turnMetronomeON()
                },
                onStop = { FluidSynthManager.turnMetronomeOff() },
                IconStart = Icons.Filled.Timer,
                Iconend = Icons.Filled.Timer,
            )
            SaveButtonDialog(filepath)
        }
    }
}

@Composable
fun LinearDeterminateIndicator(viewModel: MetronomeViewModel) {
    val count by viewModel.count.collectAsState()
    val progress = count.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing) // 300ms 平滑过渡
    )


    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
    )
}


@Composable
fun SaveButtonDialog(
     Path : File
) {
    var showDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("output") } // 默认文件名

    // 按钮：点击后弹出对话框
    Button(
        shape = RectangleShape,
        modifier = Modifier.padding(2.dp),
        contentPadding = PaddingValues(8.dp),
        onClick = { showDialog = true }) {
        Icon(
            imageVector = Icons.Filled.Save,
            contentDescription = "",
        )
    }

    // 对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("保存文件") },
            text = {
                Column {
                    Text("请输入文件名称：")
                    TextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    FluidSynthManager.SaveToWav(fileName, Path.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        destroyFluidSynthLoop()  // 运行在后台线程
                    }
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Preview
@Composable
private fun BottonsPrev() {
    val fakeFile = File("/storage/emulated/0/Music")
    Buttons(filepath = fakeFile)
}