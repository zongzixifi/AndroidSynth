package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SynthScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath: File) {

    LaunchedEffect(Unit) {
        FluidSynthManager.initialize() //初始化FluidSynth
    }
    DisposableEffect(Unit) {
        onDispose {
            FluidSynthManager.shutdown() //注销FluidSynth
            onDispose { }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        DualLayerScreen(metronomeViewModel = metronomeViewModel, filepath = filepath)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualLayerScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath : File) {
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
                repeat(20) {
                    Spacer(modifier = Modifier.padding(20.dp))
                    DrumSet()
                    Spacer(Modifier.padding(2.dp))
                    VerticalReorderList()
                }
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
            LinearDeterminateIndicator(viewModel = metronomeViewModel)
            Spacer(Modifier.padding(5.dp))
            Buttons(filepath =  filepath, viewModel = metronomeViewModel)
            Spacer(Modifier.padding(5.dp))
            BasicMusicInfoSet(modifier = Modifier.weight(1f))
            Spacer(Modifier.padding(5.dp))
            Keyboards( modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            )
            Spacer(Modifier.padding(2.dp))
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    val file =  File("/storage/emulated/0/Music")
    Project2Theme {
        SynthScreen(metronomeViewModel = fakeViewModel, filepath = file)
    }
}