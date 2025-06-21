package com.example.project2.SynthPage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.project2.FluidSynthManager
import com.example.project2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


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

    fun clearAllDrumNotes() {
        val currentMap = _drumStateMap.value
        for ((key, value) in currentMap) {
            if (value) {
                val (timeNum, note) = key
                FluidSynthManager.delDrumNote(note, timeNum)
            }
        }
        _drumStateMap.value = emptyMap()
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
    triggeredColorRes: Int
) {
    val timeNum = clapNum * 4
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { offset ->
            val triggered by drumViewModel.drumStateMap.collectAsState()
            val isTriggered = triggered[(timeNum + offset) to note] == true
            ToggleButtonWithColor(
                textStart = (timeNum + offset).toString(),
                textend = "",
                isTriggered = isTriggered,
                triggeredColor = colorResource(id = triggeredColorRes),
                untriggeredColor = colorResource(id=R.color.d0),
                onToggle = {
                    drumViewModel.toggleDrumNote(timeNum + offset, note, svel)
                }
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
    drumIconResId: Int, // 改为图片资源ID
    drumViewModel: DrumViewModel,
    triggeredColorRes: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp), // 建议设置固定高度
            painter = painterResource(id = drumIconResId),
            contentDescription = null,
//            contentScale = ContentScale.Fit // 按需调整缩放
        )
        Spacer(Modifier.padding(8.dp))
        clapList.forEach { clapNum ->
            DrumEachClapItem(
                clapNum = clapNum,
                note = note,
                svel = svel,
                drumViewModel = drumViewModel,
                triggeredColorRes = triggeredColorRes
            )
        }
    }
}

@Composable
fun DrumSet(modifier: Modifier = Modifier, drumViewModel: DrumViewModel) {
    Card (
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // 设置为透明背景
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
    ){
        Column(
            modifier = modifier
                .padding(horizontal = 2.dp, vertical = 4.dp).fillMaxWidth()
        ) {
            Spacer(Modifier.padding(4.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 35,
                svel = 100,
                drumIconResId = R.drawable.d1,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d1
            )
            Spacer(Modifier.padding(4.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 38,
                svel = 100,
                drumIconResId = R.drawable.d2,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d2
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 45,
                svel = 100,
                drumIconResId = R.drawable.d3,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d3
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 51,
                svel = 100,
                drumIconResId = R.drawable.d4,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d4
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 42,
                svel = 100,
                drumIconResId = R.drawable.d5,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d5
            )

        }
    }
}

@Composable
fun lazyRowDrumSet(modifier: Modifier = Modifier, drumViewModel: DrumViewModel) {
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
                containerColor = Color.Transparent
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
                            drumIconResId = R.drawable.d1,
                            drumViewModel = drumViewModel,
                            triggeredColorRes = R.color.d1
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 38,
                            svel = 100,
                            drumIconResId = R.drawable.d2,
                            drumViewModel = drumViewModel,
                            triggeredColorRes = R.color.d2
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 45,
                            svel = 100,
                            drumIconResId = R.drawable.d3,
                            drumViewModel = drumViewModel,
                            triggeredColorRes = R.color.d3
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 51,
                            svel = 100,
                            drumIconResId = R.drawable.d4,
                            drumViewModel = drumViewModel,
                            triggeredColorRes = R.color.d4
                        )
                        Spacer(Modifier.height(spacerLength))
                        DrumEachDrumSetItem(
                            modifier = Modifier,
                            clapList = listOf(0, 1, 2, 3),
                            note = 42,
                            svel = 100,
                            drumIconResId = R.drawable.d5,
                            drumViewModel = drumViewModel,
                            triggeredColorRes = R.color.d5
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
    triggeredColor: Color = MaterialTheme.colorScheme.primary,  // 新增参数
    untriggeredColor: Color = MaterialTheme.colorScheme.secondary, // 新增参数
    onToggle: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(8.dp),
        onClick = { onToggle() },
        modifier = modifier
            .size(width = 40.dp, height = 40.dp)
            .padding(1.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) triggeredColor else untriggeredColor
        )
    ) {
        Text(text = if (isTriggered) textStart else textend)
    }
}
