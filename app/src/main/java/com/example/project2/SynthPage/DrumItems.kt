package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.project2.R
import kotlin.math.max
import kotlin.math.round


open class DrumViewModel : ViewModel() {
    private val _drumStateMap = MutableStateFlow<Map<Pair<Int, Int>, Boolean>>(emptyMap())
    val drumStateMap: StateFlow<Map<Pair<Int, Int>, Boolean>> = _drumStateMap

    fun toggleDrumNote(timeNum: Int, note: Int, svel: Int) {
        android.util.Log.d("DrumViewModel", "toggleDrumNote: timeNum=$timeNum, note=$note, svel=$svel")

        val key = timeNum to note
        val current = _drumStateMap.value[key] ?: false
        val newState = !current

        _drumStateMap.update { it + (key to newState) }

        if (newState) {
            FluidSynthManager.setDrumNote(note, timeNum, svel)
        } else {
            FluidSynthManager.delDrumNote(note, timeNum)
        }
    }

    fun isNoteTriggered(timeNum: Int, note: Int): Boolean {
        return _drumStateMap.value[timeNum to note] ?: false
    }
}


@Composable
fun DrumEachClapItem(
    modifier: Modifier = Modifier,
    clapNum: Int,
    note: Int,
    svel :Int,
    drumViewModel: DrumViewModel,
    buttonSize: Dp
) {
    val timeNum = clapNum * 4
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { offset ->
            val triggered by drumViewModel.drumStateMap.collectAsState()
            val isTriggered = triggered[(timeNum + offset) to note] == true
            ToggleButtonWithColor(
                textStart = (timeNum + offset).toString(),
                textend = "",
                isTriggered = isTriggered,
                onToggle = {
                    drumViewModel.toggleDrumNote(timeNum + offset, note, svel)
                },
                buttonSize = buttonSize
            )
        }
    }
}


@Composable
fun DrumEachDrumSetItem(
    modifier: Modifier = Modifier,
    clapList: List<Int>,
    note: Int,
    svel: Int,
    Drums: String,
    drumViewModel: DrumViewModel,
    buttonSize: Dp = 40.dp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            modifier = Modifier.width(40.dp),
            text = Drums
        )
        Spacer(Modifier.width(5.dp))
        clapList.forEach { clapNum ->
            DrumEachClapItem(
                clapNum = clapNum,
                note = note,
                svel = svel,
                drumViewModel = drumViewModel,
                buttonSize = buttonSize
            )
        }
    }
}

@Composable
fun DrumSet(modifier: Modifier = Modifier, drumViewModel: DrumViewModel) {
    BoxWithConstraints(modifier = modifier) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight
        val rowCount = 6
        val columnCount = 16
        var buttonSize = (totalWidth / columnCount) - 4.dp
        if (buttonSize < 40.dp) buttonSize = 40.dp
        val eachHeight = totalHeight / rowCount
        val spacerLength = eachHeight - buttonSize

        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
        ) {
            LazyRow(
                modifier = Modifier.padding(8.dp).fillMaxWidth()
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 2.dp).fillMaxSize()
                    ) {
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 35,
                            svel = 100,
                            Drums = "Boom",
                            drumViewModel = drumViewModel,
                            buttonSize = buttonSize
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 38,
                            svel = 100,
                            Drums = "Clap",
                            drumViewModel = drumViewModel,
                            buttonSize = buttonSize
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 45,
                            svel = 100,
                            Drums = "Tom",
                            drumViewModel = drumViewModel,
                            buttonSize = buttonSize
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 51,
                            svel = 100,
                            Drums = "Crash",
                            drumViewModel = drumViewModel,
                            buttonSize = buttonSize
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 42,
                            svel = 100,
                            Drums = "Hats",
                            drumViewModel = drumViewModel,
                            buttonSize = buttonSize
                        )
                    }
                }
            }
        }
    }
}
@Preview
@Composable
private fun DrumPrev(modifier: Modifier = Modifier.fillMaxSize()) {
    val fakeViewModel = object : DrumViewModel() {
    }
    Surface(modifier= Modifier.background(color = Color.Gray),
        color = MaterialTheme.colorScheme.primary,
        ) {
        DrumSet(modifier = Modifier.height(250.dp), fakeViewModel)
    }
}

@Composable
fun ToggleButtonWithColor(
    modifier: Modifier = Modifier,
    textStart: String,
    textend: String,
    isTriggered: Boolean,
    onToggle: () -> Unit,
    buttonSize: Dp
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .clickable { onToggle() }
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        val painter = if (isTriggered) painterResource(id = R.drawable.drum_key_selected) else painterResource(id = R.drawable.drum_key_unselected)
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(buttonSize).clip(shape = RoundedCornerShape(5.dp)),
        )
    }
}
