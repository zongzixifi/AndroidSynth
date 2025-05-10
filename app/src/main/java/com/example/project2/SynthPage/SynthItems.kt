package com.example.project2.SynthPage

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.project2.SynthPage.KeyboardsItem
import com.example.project2.SynthPage.MusicViewModel
import com.example.project2.SynthPage.getMidiFromRootNote
import com.example.project2.SynthPage.getScaleNotes
import kotlinx.coroutines.delay


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt


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


    val midiNotes = getScaleNotes(rootMidi = rootKey, mode = mode, noteCount = keyInterval)
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
                                val num = (normalizedX * keyInterval).toInt()

                                key = CountKeyAndVel(num, midiNotes)
                                vel = (baseVel + normalizedY * 117).toInt()

                                keyStates[num] = true

                                FluidSynthManager.playNote(key, vel, channel)
                                true
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val normalizedX =
                                    if (componentSize.width > 0) event.x / componentSize.width else 0f
                                val normalizedY =
                                    if (componentSize.height > 0) event.y / componentSize.height else 0f
                                val num = (normalizedX * keyInterval).toInt()

                                temp_key = CountKeyAndVel(num, midiNotes)
                                temp_vel = (baseVel + normalizedY * 87).toInt()

                                keyStates.replaceAll { false }
                                keyStates[num] = true

                                if (temp_vel != vel || key != temp_key) { //过滤抖动减少颤音
                                    FluidSynthManager.stopNoteDelay(key, channel)
                                    if (temp_vel - vel > 5 || temp_vel - vel < -5 || key != temp_key) {
                                        key = temp_key
                                        vel = temp_vel
                                        FluidSynthManager.playNote(key, vel, channel)
                                    }
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
                KeyboardsItem(count = keyInterval, keyStates = keyStates, modifier = Modifier.matchParentSize())
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
fun KeyboardsItem(modifier: Modifier = Modifier, count: Int, keyStates: List<Boolean>) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ){
        repeat(count) { index ->
            // 确保keyStates有足够的元素
            val isPressed = if (index < keyStates.size) keyStates[index] else false
            KeyboardsBubble(
                modifier = Modifier.weight(1f),
                isPressed = isPressed
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
fun KeyboardsBubble(modifier: Modifier = Modifier, isPressed: Boolean = false) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 0.dp, horizontal = 2.dp)
            .graphicsLayer {
                // 根据按键状态调整透明度和缩放
                alpha = if (isPressed) 0.8f else 1.0f
                scaleX = if (isPressed) 0.95f else 1.0f
                scaleY = if (isPressed) 0.95f else 1.0f
            },
        color = if (isPressed) {
            // 按下时使用强调色或稍微变化的颜色
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primary
        },
        shape = RoundedCornerShape(10.dp),
    ){
        Button(
            onClick = {},
            enabled = false, // 禁用按钮点击，我们使用外层的触摸处理
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        ){
            // 如果需要在按键上显示图标或文字，可以在这里添加
        }
    }
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
    // 创建一个固定的状态列表，模拟第3个和第7个按键被按下
    val pressedKeys = remember {
        MutableList(16) { index -> index == 2 || index == 6 } // 索引从0开始，所以2和6对应第3和第7个按键
    }

    KeyboardsItem(
        count = 16,
        keyStates = pressedKeys
    )
}