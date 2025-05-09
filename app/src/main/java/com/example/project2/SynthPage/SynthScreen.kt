package com.example.project2.SynthPage

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
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

    val activity = LocalActivity.current
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val config = LocalConfiguration.current
    Box(modifier = modifier.fillMaxSize()) {
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FullscreenDrumScreen(
                modifier = Modifier
                    .fillMaxSize()
                ,
                drumViewModel = drumViewModel,
                onClickBackToFullscreenDrum = {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                },
                metronomeViewModel = metronomeViewModel
            )
        }else{
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DualLayerScreen(
                    metronomeViewModel = metronomeViewModel,
                    filepath = filepath,
                    onClickJumpToFullscreenDrum = {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    },
                    drumViewModel = drumViewModel
                )
            }
        }

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
                    .height(450.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier =Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ){
                    IconButton(
                        onClick = { drumViewModel.clearAllDrumNotes() },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "清除鼓机"
                        )
                    }
                    IconButton(
                        onClick = onClickJumpToFullscreenDrum,
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitScreen,
                            contentDescription = "打开全屏打击垫"
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                lazyRowDrumSet(drumViewModel = drumViewModel)
                Spacer(Modifier.height(10.dp))
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
            BasicMusicInfoSet(modifier = Modifier
                .weight(1f)
                .padding(start = 28.dp, end = 28.dp, top = 28.dp))
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
    modifier: Modifier = Modifier,
    onClickBackToFullscreenDrum: () -> Unit = {},
    drumViewModel: DrumViewModel,
    metronomeViewModel: MetronomeViewModel
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 30.dp)) {
        Row(
            modifier =Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                onClick = onClickBackToFullscreenDrum,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "返回"
                )
            }
            IconButton(
                onClick = { drumViewModel.clearAllDrumNotes() },
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "清除鼓机"
                )
            }
        }
        LinearDeterminateIndicator(viewModel = metronomeViewModel, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(5.dp))
        DrumSet(drumViewModel = drumViewModel)
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
        FullscreenDrumScreen(drumViewModel = fakeViewModel2, metronomeViewModel = fakeViewModel)
    }
}