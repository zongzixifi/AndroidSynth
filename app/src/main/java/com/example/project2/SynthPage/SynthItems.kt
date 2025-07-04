package com.example.project2.SynthPage


import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.R


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Keyboards(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val musicInfo by viewModel.musicInfo.collectAsState()
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var channel by remember { mutableStateOf(1) }
    var key by remember { mutableStateOf(0) }
    var vel by remember { mutableStateOf(0) }
    var temp_key by remember { mutableStateOf(0) }
    var temp_vel by remember { mutableStateOf(0) }
    val baseVel = 40
    var keyInterval by remember { mutableStateOf(12) }
    val mode = musicInfo.scale
    var octave by remember { mutableStateOf(4) }
    val rootKey = getMidiFromRootNote(musicInfo.root, octave = octave)

    val keyStates = remember { mutableStateListOf<Boolean>().apply { repeat(12) { add(false) } } }


//    val midiNotes = getScaleNotes(rootMidi = rootKey, mode = mode, noteCount = keyInterval)
    val midiNotes = remember(musicInfo.root, musicInfo.scale, octave) {
        getScaleNotes(
            rootMidi = getMidiFromRootNote(musicInfo.root, octave),
            mode = musicInfo.scale,
            noteCount = keyInterval
        )
    }
    Surface(
        modifier = modifier
            .height( 300.dp),
        color = colorResource(id = R.color.gray),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                modifier = Modifier.height(5.dp).weight(1f),
                value = keyInterval.toFloat(),
                valueRange = 5f..12f,
//                steps = 24,
                onValueChange = { keyInterval = it.toInt()},
                thumb = {
                    Image(
                        painter = painterResource(id = R.drawable.o),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFAEB9D4),
                    inactiveTrackColor = Color(0xFFCCCCCC)
                )
            )
            Slider(
                modifier = Modifier.height(5.dp).weight(1f),
                value = octave.toFloat(),
                valueRange = 1f..8f,
//                steps = 8,
                onValueChange = { octave = it.toInt()},
                thumb = {
                    Image(
                        painter = painterResource(id = R.drawable.o),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFAEB9D4),
                    inactiveTrackColor = Color(0xFFCCCCCC)
                )
            )
            Box(
                modifier = Modifier
                    .weight(6f)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .fillMaxWidth()
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
                                // 越界保护
                                val num = (normalizedX * keyInterval).toInt().coerceIn(0, keyInterval - 1)
                                key = CountKeyAndVel(num, midiNotes)
                                vel = (baseVel + normalizedY * 117).toInt().coerceIn(0, 127)

                                keyStates[num] = true

                                FluidSynthManager.playNote(key, vel, channel)
                                true
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val normalizedX =
                                    if (componentSize.width > 0) event.x / componentSize.width else 0f
                                val normalizedY =
                                    if (componentSize.height > 0) event.y / componentSize.height else 0f
                                // 越界保护
                                val num = (normalizedX * keyInterval).toInt().coerceIn(0, keyInterval - 1)
                                temp_key = CountKeyAndVel(num, midiNotes)
                                temp_vel = (baseVel + normalizedY * 87).toInt().coerceIn(0, 127)

                                keyStates.replaceAll { false }
                                keyStates[num] = true

                                // 无论是否更新，都执行 stopNoteDelay，确保滑动时音符及时停止
                                FluidSynthManager.stopNoteDelay(key, channel)

                                // 仅在变化较大时重新触发 playNote，避免重复播放同一音符
                                if (temp_vel - vel > 5 || temp_vel - vel < -5 || key != temp_key) {
                                    key = temp_key
                                    vel = temp_vel
                                    FluidSynthManager.playNote(key, vel, channel)
                                }
                                true
                            }

                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                FluidSynthManager.stopNote(key, channel)

                                keyStates.replaceAll { false }

                                true
                            }

                            else -> false
                        }
                    },
            ) {
//                KeyboardsItem(count = keyInterval, modifier = Modifier.matchParentSize())
//                KeyboardsItem(count = keyInterval, keyStates = keyStates, modifier = Modifier.matchParentSize())
                KeyboardsItem(
                    count = keyInterval,
                    keyStates = keyStates,
                    midiNotes = midiNotes, // 新增参数
                    modifier = Modifier.matchParentSize()
                )

            }
        }
    }
}

//@Composable
//fun KeyboardsItem(modifier: Modifier = Modifier, count: Int) {
//    Row(
//        modifier = modifier,
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceEvenly,
//    ){
//        repeat(count){
//            KeyboardsBubble(modifier = Modifier.weight(1f))
//        }
//    }
//}

@Composable
fun KeyboardsItem(
    modifier: Modifier = Modifier,
    count: Int,
    keyStates: List<Boolean>,
    midiNotes: List<Int> // 接收 MIDI 音符列表
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(count) { index ->
            val midiNote = if (index < midiNotes.size) midiNotes[index] else 60 // 默认值 60（C4）
            KeyboardsBubble(
                modifier = Modifier.weight(1f),
                isPressed = keyStates.getOrElse(index) { false },
                midiNote = midiNote // 传递 MIDI 音符
            )
        }
    }
}

//@Composable
//fun KeyboardsBubble(modifier: Modifier = Modifier) {
//    Surface(
//        modifier = modifier
//            .fillMaxHeight()
//            .padding(vertical = 0.dp, horizontal = 2.dp)
//        ,
//        color = MaterialTheme.colorScheme.onSurface,
//        shape = RoundedCornerShape(10.dp),
//    ){
//        Button(
//            onClick = {}
//        ){
//
//        }
//    }
//}


@Composable
fun KeyboardsBubble(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    midiNote: Int
) {
    // 获取主题色
    val themeColor = MaterialTheme.colorScheme.primary
    // 计算动态亮度
    val lightness = 0.5f + (midiNote.toFloat() / 127) * 0.5f

    // 根据主题色和动态亮度调整颜色
    val dynamicColor = themeColor.copy(alpha = lightness)

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.8f else 1.0f
                scaleX = if (isPressed) 0.95f else 1.0f
                scaleY = if (isPressed) 0.95f else 1.0f
            },
        color = if (isPressed) dynamicColor.darker() else dynamicColor, // 使用 dynamicColor
        shape = RoundedCornerShape(10.dp)
    ) {
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        ) {}
    }
}

// 扩展函数：使颜色变暗
fun Color.darker(factor: Float = 0.7f): Color {
    return this.copy(alpha = this.alpha * factor)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ListNumberPicker(
    data: List<T>,
    selectIndex: Int,
    visibleCount: Int,
    modifier: Modifier = Modifier,
    onSelect: (index: Int, item: T) -> Unit,
    content: @Composable (item: T) -> Unit,
) {
    BoxWithConstraints(modifier = modifier, propagateMinConstraints = true) {
        val pickerHeight = maxHeight
        val size = data.size
        val itemHeight = pickerHeight / visibleCount
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = selectIndex
        )
        val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LazyColumn(
            modifier = Modifier,
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
        ) {
            //占据相应的高度比如显示5个那么中间那个是选中的其他的就是非选中，但也要占据一定的空间。
            for (i in 1..visibleCount / 2) {
                item {
                    Surface(modifier = Modifier.height(itemHeight)) {}
                }
            }
            items(size) { index ->
                //预防滑动的时候出现数组越界
                if (firstVisibleItemIndex >= size) {
                    onSelect(size - 1, data[size - 1])
                } else {
                    onSelect(firstVisibleItemIndex, data[firstVisibleItemIndex])
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    content(data[index])
                }
            }
            for (i in 1..visibleCount / 2) {
                item {
                    Surface(modifier = Modifier.height(itemHeight)) {}
                }
            }
        }

    }
}


@Preview
@Composable
private fun BasicMusicInfoSetPrev() {
    BasicMusicInfoSet()
}



@Preview
@Composable
private fun KeyboardsItemPrev() {
    Keyboards()
}

//@Preview
//@Composable
//private fun KeyboardsPrev() {
//    KeyboardsItem(count = 16)
//}

@Preview
@Composable
private fun KeyboardsPrev() {
    // 1. 创建模拟的按压状态列表
    val pressedKeys = remember {
        MutableList(16) { index -> index == 2 || index == 6 }
    }

    // 2. 创建模拟的MIDI音符列表（示例：C大调音阶）
    val midiNotes = remember {
        (0 until 16).map { 60 + it } // 从C4（MIDI 60）开始连续16个音
    }

    // 3. 传入midiNotes参数
    KeyboardsItem(
        count = 16,
        keyStates = pressedKeys,
        midiNotes = midiNotes // 新增参数
    )
}