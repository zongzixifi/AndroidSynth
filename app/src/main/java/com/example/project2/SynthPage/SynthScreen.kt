package com.example.project2.SynthPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SynthScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath: File) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            LinearDeterminateIndicator(metronomeViewModel)
            Buttons(filepath =  filepath)
            BasicMusicInfoSet()
            Keyboards( modifier = Modifier
                .heightIn(min = 10.dp, max = 300.dp),
            )

            DrumSet()
            VerticalReorderList()
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    Project2Theme {
        //SynthScreen(metronomeViewModel = fakeViewModel, filepath = file)
    }
}