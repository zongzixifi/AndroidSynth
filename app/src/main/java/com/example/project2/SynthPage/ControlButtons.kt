

package com.example.project2.SynthPage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

import androidx.compose.ui.input.pointer.pointerInput
import com.bumptech.glide.load.resource.bitmap.CircleCrop


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
    var isPressed by remember { mutableStateOf(false) }

    // 按压效果：缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    // 按压效果：透明度动画
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

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
            .padding(0.dp)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent // 无背景颜色
        )
    ) {
        Icon(
            imageVector = if (isTriggered) IconStart else Iconend,
            contentDescription = "",
            tint = if (isTriggered) ColorOnStart else ColorOnEnd
        )
    }
}


// 新的小尺寸图标按钮组件
@Composable
fun TinyIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    var isPressed by remember { mutableStateOf(false) }

    // 按压效果：缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    // 按压效果：透明度动画
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    Button(
        shape = CircleShape,
        onClick = onClick,
        modifier = modifier
//            .size(100.dp)
//            .width(50.dp)
//            .height(50.dp)
            .padding(0.dp)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent // 无背景颜色
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = tint
        )
    }
}


@Composable
fun SimpleIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    var isPressed by remember { mutableStateOf(false) }

    // 按压效果：缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    // 按压效果：透明度动画
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    Button(
        shape = RectangleShape,
        onClick = onClick,
        modifier = modifier
            .padding(2.dp)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent // 无背景颜色
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = tint
        )
    }
}

@Composable
fun Buttons(
    modifier: Modifier = Modifier.fillMaxWidth() // 使 Card 横向最大化
        .padding(horizontal = 0.dp), // 移除水平内边距,
    filepath: File,
    viewModel : MetronomeViewModel,
    onClickJumpFrontScreen: () -> Unit ={}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // 设置为透明背景
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 第一行：左边是返回按钮，右边依次是清除和保存
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧返回按钮
                SimpleIconButton(
                    onClick = onClickJumpFrontScreen,
                    icon = Icons.Filled.ArrowBackIosNew
                )

                // 右侧按钮容器（清除+保存）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 使用新的 TinyIconButton
                    TinyIconButton(
                        onClick = { FluidSynthManager.clearLoop() },
                        icon = Icons.Filled.DeleteOutline
                    )
                    SaveButtonDialog(
                        Path = filepath,
                        viewModel = viewModel
                    )
                }
            }

            // 第二行：中间三个依次是 录制、播放、节拍器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), // 保持或增加第二行的间距
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToggleButtonIcon(
                    modifier = Modifier.padding(0.dp),
                    onStart = {
                        FluidSynthManager.startRecording()
                    },
                    onStop = { FluidSynthManager.stopRecording() },
                    IconStart = Icons.Filled.Mic,
                    Iconend = Icons.Filled.Mic,
                    ColorOnStart = Color.Red,
                    ColorOnEnd = Color.Gray,
                )
                ToggleButtonIcon(
                    modifier = Modifier.padding(0.dp),
                    onStart = {
                        FluidSynthManager.startPlayback()
                    },
                    onStop = { FluidSynthManager.stopPlayback() },
                    IconStart = Icons.Filled.Pause,
                    Iconend = Icons.Outlined.PlayArrow
                )
                ToggleButtonIcon(
                    modifier = Modifier.padding(0.dp),
                    onStart = {
                        FluidSynthManager.turnMetronomeON()
                    },
                    onStop = { FluidSynthManager.turnMetronomeOff() },
                    IconStart = Icons.Filled.Timer,
                    Iconend = Icons.Filled.Timer,
                )
            }
        }
    }
}

@Composable
fun LinearDeterminateIndicator(
    modifier: Modifier = Modifier,
    viewModel: MetronomeViewModel
) {
    val count by viewModel.count.collectAsState()
    val progress = count.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing) // 300ms 平滑过渡
    )


    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp),
    )
}


@Composable
fun SaveButtonDialog(
    Path : File,
    viewModel: MetronomeViewModel,
    modifier: Modifier = Modifier // 添加modifier参数
) {
    var showDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("output") }

    SimpleIconButton(
        modifier = modifier,
        onClick = { showDialog = true },
        icon = Icons.Filled.Save
    )

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
                    showProgressDialog = true // 显示进度对话框

                    CoroutineScope(Dispatchers.IO).launch {
                        FluidSynthManager.SaveToWav(fileName, Path.toString())
                        withContext(Dispatchers.IO) {
                            destroyFluidSynthLoop()
                        }
                        withContext(Dispatchers.Main) {
                            showProgressDialog = false
                            showSuccessDialog = true
                            showDialog = false
                        }
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

    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { /* 禁止关闭进度对话框 */ },
            title = { Text("保存中...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("正在保存文件，请稍候...")
                    LinearDeterminateIndicator(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        viewModel = viewModel
                    )
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("保存完成") },
            text = { Text("文件已成功保存！") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                }) {
                    Text("确定")
                }
            }
        )
    }
}

@Preview
@Composable
private fun BottonsPrev() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    val fakeFile = File("/storage/emulated/0/Music")
    Buttons(filepath = fakeFile, viewModel = fakeViewModel)
}
