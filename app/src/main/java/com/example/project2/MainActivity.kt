package com.example.project2

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.project2.ChatScreen.ChatViewModel
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
    external fun stopThread()
    external fun turnMetronomeON()
    external fun destoryFluidSynth()
    external fun turnMetronomeOff()
    external fun setDrumNote(note:Int, timeNum:Int , svel :Int)
    external fun delDrumNote(note:Int, timeNum:Int)

    private var audioTrack: AudioTrack? = null

    fun playAudio(audioData: ShortArray) {
        if (audioTrack == null) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
        }

        audioTrack!!.write(audioData, 0, audioData.size)
        audioTrack!!.play()
    }

    fun initialize() {
        createFluidSynth() // 初始化 FluidSynth
    }

    fun shutdown() {
        stopPlayback()  // 停止回放
        stopRecording() // 停止录制
        stopThread()    // 终止后台线程
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
fun DrumEachClapItem(
    modifier: Modifier = Modifier,
    clapNum: Int,
    note: Int,
    svel :Int
) {
    val timeNum = clapNum*4
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 1, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 1, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 2, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 2, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 3, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 3, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
    }
}

@Composable
fun DrumEachDrumSetItem(modifier: Modifier = Modifier,  clapList : List<Int>, note : Int, svel :Int) {

    Column {
        clapList.forEach { clapNum ->
            DrumEachClapItem(
                clapNum = clapNum,
                note = note,
                svel = svel
            )
        }
    }
}

@Composable
fun DrumSet(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ){
        DrumEachDrumSetItem(
            modifier = Modifier,
            clapList = listOf(0, 1, 2, 3),
            note = 38,
            svel = 100
        )
    }
}

@Preview
@Composable
private fun DrumPrev() {
    DrumSet()
}

@Composable
fun ToggleButtonWithColor(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onStop: () -> Unit,
    textStart: String,
    textend: String,
    ) {
    var isTriggered by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isTriggered = !isTriggered
            if (isTriggered) {
                onStart()
            } else {
                onStop()
            }
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = if (isTriggered) textStart else textend)
    }
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
            ToggleButtonWithColor(
                onStart = {
                    FluidSynthManager.startRecording()
                          },
                onStop = { FluidSynthManager.stopRecording() },
                textStart = "rec",
                textend = "Stop rec"
            )
            Button(onClick = { FluidSynthManager.startOverdub() }) {
                Text(text = "Start Overdub")
            }
            ToggleButtonWithColor(
                onStart = {
                    FluidSynthManager.startPlayback()
                },
                onStop = { FluidSynthManager.stopPlayback() },
                textStart = "Start play",
                textend = "Stop Playback"
            )
            Button(onClick = { FluidSynthManager.clearLoop() }) {
                Text(text = "Clear Loop")
            }
            Button(onClick = { FluidSynthManager.turnMetronomeON() }) {
                Text(text = "metornome")
            }
            ToggleButtonWithColor(
                onStart = {
                    FluidSynthManager.turnMetronomeON()
                },
                onStop = { FluidSynthManager.turnMetronomeOff() },
                textStart = "MetronomeOff",
                textend = "MetronomeON"
            )
            Text(text = "PlayHere")
            PlayZone( modifier = Modifier
                .heightIn(min = 10.dp, max = 200.dp),
                )

            DrumSet()
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