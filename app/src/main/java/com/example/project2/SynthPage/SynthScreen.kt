package com.example.project2.SynthPage

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.R
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SynthScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath: File, onClickJumpToFullscreenDrum: () -> Unit ={}, drumViewModel: DrumViewModel) {

    LaunchedEffect(Unit) {
        FluidSynthManager.initialize() //初始化FluidSynth
    }
    DisposableEffect(Unit) {
        onDispose {
            FluidSynthManager.shutdown() //注销FluidSynth
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        DualLayerScreen(metronomeViewModel = metronomeViewModel, filepath = filepath, onClickJumpToFullscreenDrum = onClickJumpToFullscreenDrum, drumViewModel = drumViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualLayerScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath : File, onClickJumpToFullscreenDrum: () -> Unit ={}, drumViewModel: DrumViewModel) {
    val sheetState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 80.dp, // 默认展示部分高度
        sheetContent = {
            // 上层滑动层
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onClickJumpToFullscreenDrum,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitScreen,
                        contentDescription = "打开全屏打击垫"
                    )
                }
                Spacer(modifier = Modifier.padding(20.dp))
                DrumSet(drumViewModel = drumViewModel)
                Spacer(Modifier.padding(2.dp))
                VerticalReorderList()
            }
        }
    ) {innerPadding ->
        // 底层背景页面
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Gray),
        ) {
            LinearDeterminateIndicator(viewModel = metronomeViewModel, modifier = Modifier.padding(top = 10.dp))
            Buttons(filepath =  filepath, viewModel = metronomeViewModel, modifier = Modifier.padding(horizontal = 28.dp))
            BasicMusicInfoSet(modifier = Modifier.weight(1f).padding(start = 28.dp, end = 28.dp, top = 28.dp))
            Keyboards( modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .padding(28.dp)
                ,
            )
            Spacer(Modifier.padding(2.dp))
        }
    }
}


@Composable
fun FullscreenDrumScreen(
    onClickBackToFullscreenDrum: () -> Unit = {},
    drumViewModel: DrumViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Row(modifier = Modifier.fillMaxSize()) {
        DrumSet(drumViewModel = drumViewModel)
        Button(
            onClick = onClickBackToFullscreenDrum,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("返回")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    val fakeViewModel2 = object : DrumViewModel() {
    }
    val file =  File("/storage/emulated/0/Music")
    Project2Theme {
        SynthScreen(metronomeViewModel = fakeViewModel, filepath = file, drumViewModel = fakeViewModel2)
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenDrumPreview() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    val fakeViewModel2 = object : DrumViewModel() {
    }
    val file =  File("/storage/emulated/0/Music")
    Project2Theme {
        FullscreenDrumScreen(drumViewModel = fakeViewModel2)
    }
}