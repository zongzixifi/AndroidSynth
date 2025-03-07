package com.example.project2

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.project2.ChatScreen.ChatViewModel
import com.example.project2.SynthPage.BasicMusicInfoSet
import com.example.project2.SynthPage.DrumSet
import com.example.project2.SynthPage.Keyboards
import com.example.project2.SynthPage.ToggleButtonWithColor
import com.example.project2.SynthPage.VerticalReorderList
import com.example.project2.ui.theme.Project2Theme
import java.io.File
import java.io.FileOutputStream



fun copySoundFontToInternalStorage(context: Context): String {
    val assetManager = context.assets
    val inputStream = assetManager.open("GeneralUser-GS.sf2")
    val outFile = File(context.filesDir, "soundfont.sf2")
    val outputStream = FileOutputStream(outFile)

    inputStream.copyTo(outputStream)
    inputStream.close()
    outputStream.close()

    return outFile.absolutePath
}

object FluidSynthManager {

    init {
        System.loadLibrary("project2") // 加载 native 库
    }

    external fun createFluidSynth(): String
    external fun playNote(note: Int, vel: Int, channel: Int)
    external fun stopNote(note: Int, channel: Int)
    external fun stopNoteDelay(note: Int, channel: Int)
    external fun startRecording()
    external fun stopRecording()
    external fun stopPlayback()
    external fun startOverdub()
    external fun clearLoop()
    external fun startPlayback()
    external fun turnMetronomeON()
    external fun destoryFluidSynth()
    external fun turnMetronomeOff()
    external fun setDrumNote(note:Int, timeNum:Int , svel :Int)
    external fun delDrumNote(note:Int, timeNum:Int)
    external fun setChordNote(note:Int, timeNum:Int , svel :Int, ClapOnCount: Int)
    external fun delChordNote(note:Int, timeNum:Int)
    external fun delAllChordNote()
    external fun setBasicMusicInfo(BPM : Int, bar : Int, clap : Int,)

    fun initialize() {
        createFluidSynth() // 初始化 FluidSynth
    }

    fun shutdown() {
        stopPlayback()  // 停止回放
        stopRecording() // 停止录制
        destoryFluidSynth()
        // 释放 FluidSynth 资源
        System.gc() // 强制回收
    }
}


class MainActivity : ComponentActivity() {

    private val chatViewModel : ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FluidSynthManager.initialize() //初始化FluidSynth
        copySoundFontToInternalStorage(this)
        //startAudio()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
////                    ChatScreen(
////                        modifier = Modifier.padding(innerPadding),
////                        chatViewModel
////                    )
//                    NavgationGraph(
//                        modifier = Modifier.padding(innerPadding),
//                        chatViewModel = chatViewModel
                     TestView(
                         modifier = Modifier
                         .padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FluidSynthManager.shutdown()
    }
}

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
    Iconend:  ImageVector,
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
fun Buttons(modifier: Modifier = Modifier) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment =  Alignment.CenterVertically
    ){
        ToggleButton(
            modifier= Modifier.padding(2.dp),
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
            modifier= Modifier.padding(2.dp),
            onStart = {
                FluidSynthManager.startPlayback()
            },
            onStop = { FluidSynthManager.stopPlayback() },
            IconStart = Icons.Filled.Pause,
            Iconend = Icons.Outlined.PlayArrow
        )
        Button(
            modifier= Modifier.padding(2.dp),
            onClick = { FluidSynthManager.clearLoop() }) {
            Text(text = "Clear Loop")
        }
        ToggleButton(
            modifier= Modifier.padding(2.dp),
            onStart = {
                FluidSynthManager.turnMetronomeON()
            },
            onStop = { FluidSynthManager.turnMetronomeOff() },
            textStart = "MetronomeOff",
            textend = "MetronomeON"
        )
    }
}


@Preview
@Composable
private fun BottonsPrev() {
    Buttons()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TestView(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            BasicMusicInfoSet()
            //Buttons()
            Text(text = "PlayHere")
            Keyboards( modifier = Modifier
                .heightIn(min = 10.dp, max = 300.dp),
                )

            DrumSet()
            VerticalReorderList()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayZone(modifier: Modifier = Modifier) {
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var channel by remember { mutableStateOf(1) }
    var key by remember { mutableStateOf(0) }
    var vel by remember { mutableStateOf(0) }
    var temp_key by remember { mutableStateOf(0) }
    var temp_vel by remember { mutableStateOf(0) }
    val baseVel = 10
    val rootKey = 60
    val keyInterval = 12
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,

    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    componentSize = layoutCoordinates.size
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        touchPosition = offset // 获取 x, y 位置
                    }
                }
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val normalizedX =
                                if (componentSize.width > 0) event.x / componentSize.width else 0f
                            val normalizedY =
                                if (componentSize.height > 0) event.y / componentSize.height else 0f
                            key = (rootKey + normalizedX * keyInterval).toInt()
                            vel = (baseVel + normalizedY * 117).toInt()
                            FluidSynthManager.playNote(key, vel, channel)
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val normalizedX =
                                if (componentSize.width > 0) event.x / componentSize.width else 0f
                            val normalizedY =
                                if (componentSize.height > 0) event.y / componentSize.height else 0f
                            temp_key =
                                (rootKey + normalizedX * keyInterval).toInt()
                            temp_vel = (baseVel + normalizedY * 117).toInt()
                            if (temp_vel - vel < 5 && temp_vel - vel > 5 && key == temp_key) {
                                FluidSynthManager.stopNoteDelay(key, channel)
                            } else {
                                FluidSynthManager.stopNoteDelay(key, channel)
                                key = temp_key
                                vel = temp_vel
                                FluidSynthManager.playNote(key, vel, channel)
                            }
                            true
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            FluidSynthManager.stopNote(key, channel)
                            true
                        }

                        else -> false
                    }
                },
        )
    }
    Text(
        modifier = Modifier.padding(10.dp),
        text = "key: ${key} vel :${vel}"
    )
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2Theme {
        TestView()
    }
}