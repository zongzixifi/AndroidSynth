package com.example.project2

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.ChatScreen.ChatViewModel
import com.example.project2.MusicGenPage.MusicGenViewModel
import com.example.project2.SynthPage.MetronomeViewModel
import com.example.project2.SynthPage.SynthScreen
import com.example.project2.ui.theme.Project2Theme
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing

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
    external fun getCount(): Double
    external fun SaveToWav(filename : String, Path : String)
    external fun destroyFluidSynthLoop()

    fun initialize() {
        createFluidSynth() // 初始化 FluidSynth
    }

    fun shutdown() {
        stopPlayback()
        stopRecording()
        destoryFluidSynth()
        // 释放 FluidSynth 资源
        System.gc()
    }
}

class MainActivity : ComponentActivity() {
    private val chatViewModel : ChatViewModel by viewModels()
    private val metronomeViewModel: MetronomeViewModel by viewModels()
    private val musicGenViewModel: MusicGenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        copySoundFontToInternalStorage(this)
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "demoMusic")
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
        val filepath = musicDir
        enableEdgeToEdge()
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                ) { innerPadding ->
                    if (filepath != null) {
                        NavgationGraph(
                            modifier = Modifier.padding(innerPadding),
                            chatViewModel = chatViewModel,
                            metronomeViewModel = metronomeViewModel,
                            musicGenViewModel = musicGenViewModel,
                            filepath = filepath,
                            context = applicationContext
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FluidSynthManager.shutdown()
    }
}
