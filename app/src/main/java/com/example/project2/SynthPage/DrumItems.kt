package com.example.project2.SynthPage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.project2.R
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
import androidx.compose.ui.res.colorResource


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
    triggeredColorRes: Int
) {
    val timeNum = clapNum * 4
//    val drumUnTriggeredColors = listOf(
//        R.color.d1,  // offset=0 → d1
//        R.color.d2,  // offset=1 → d2
//        R.color.d3,  // offset=2 → d3
//        R.color.d4   // offset=3 → d4
//    )
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
        horizontalArrangement = Arrangement.spacedBy(5.dp)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ){
        Column(
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 35,
                svel = 100,
                drumIconResId = R.drawable.d1,
                drumViewModel = drumViewModel,
                triggeredColorRes = R.color.d1
            )
            Spacer(Modifier.padding(2.dp))
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
