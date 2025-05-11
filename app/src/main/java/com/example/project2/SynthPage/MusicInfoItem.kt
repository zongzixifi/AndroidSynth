package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.launch
import kotlin.math.round


@Composable
fun BasicMusicInfoSet(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val musicInfo by viewModel.musicInfo.collectAsState()

    LaunchedEffect(musicInfo) {
        FluidSynthManager.setBasicMusicInfo(musicInfo.BPM, musicInfo.bar, musicInfo.clap)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0))
                .fillMaxHeight()
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
//            Row(
//                modifier = Modifier,
//                horizontalArrangement = Arrangement.spacedBy(1.dp)
//            ) {
//                Button(
//                    onClick = {
//                        viewModel.updateBar(1)
//                    }
//                ) { Text(text = "1 BAR") }
//                Button(
//                    onClick = {
//                        viewModel.updateBar(2)
//                    }
//                ) { Text(text = "2 BAR") }
//                Button(
//                    onClick = {
//                        viewModel.updateBar(4)
//                    }
//                ) { Text(text = "4 BAR") }
//                Button(
//                    onClick = {
//                        viewModel.updateBar(8)
//                    }
//                ) { Text(text = "8 BAR") }
//            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // 添加水平内边距
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 每个标签容器宽度与下方PickerWheel对应
                Box(
                    modifier = Modifier
                        .weight(1f) // 对应前三个PickerWheel的宽度
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BPM", fontSize = 20.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CLAP" , fontSize = 20.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ROOT", fontSize = 20.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f) // 对应最后一个PickerWheel的宽度
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SCALE", fontSize = 20.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                ,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                PickerWheel(
                    items = (40..240).toList().map { it.toString() },
                    selectedIndex = musicInfo.BPM - 40,
                    onItemSelected = {
                        viewModel.updateBPM(it + 40)
                    },
                    modifier = Modifier.weight(1f)
                )
                PickerWheel(
                    items = (3..4).toList().map { it.toString() },
                    selectedIndex = musicInfo.clap - 3,
                    onItemSelected = {
                        viewModel.updateClap(it + 3)
                    },
                    modifier = Modifier.weight(1f)
                )
                val rootItems = listOf("C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B")
                PickerWheel(
                    items = rootItems,
                    selectedIndex = rootItems.indexOf(musicInfo.root),
                    onItemSelected = {
                        viewModel.updateRoot(rootItems[it])
                    },
                    modifier = Modifier.weight(1f)
                )
                val scaleMap = linkedMapOf(
                    "major" to "Maj",
                    "minor" to "Min",
                    "blues" to "Blues",
                    "dorian" to "Dorian",
                    "major_pentatonic" to "MajPen",
                    "minor_pentatonic" to "MinPen",
                    "harmonic_minor" to "HarMin",
                    "melodic_minor" to "MelMin",
                    "phrygian" to "Phryg",
                    "lydian" to "Lydian",
                    "mixolydian" to "Mixolyd",
                    "locrian" to "Locrian"
                )

                val scaleItems = scaleMap.keys.toList()
                val scaleDisplayNames = scaleMap.values.toList()

                PickerWheel(
                    items = scaleDisplayNames,
                    selectedIndex = scaleItems.indexOf(musicInfo.scale),
                    onItemSelected = { selectedDisplayIndex ->
                        val originalCode = scaleItems[selectedDisplayIndex]
                        viewModel.updateScale(originalCode)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 滚筒选择器实现
@Composable
fun PickerWheel(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 初始滚动到选中项
    LaunchedEffect(Unit) {
        listState.scrollToItem(selectedIndex)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { visibleIndex ->
                // 当前居中项通常是第一可见项 + 1（因为中间遮罩偏下一点）
                val centerIndex = (visibleIndex).coerceIn(0, items.lastIndex)
                if (centerIndex != selectedIndex) {
                    onItemSelected(centerIndex)
                }
            }
    }

    Box(modifier = modifier.height(80.dp).background(Color(0xFF838EA8))) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
            ,
            contentPadding = PaddingValues(vertical = 20.dp), // 上下空出
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(items) { index, item ->
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                onItemSelected(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        fontSize = if (index == selectedIndex) 22.sp else 18.sp,
                        color = if (index == selectedIndex) Color.White else Color(0xA0E0E0E0),
                    )
                }
            }
        }

        // 中间高亮线
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.Center)
                .background(Color.LightGray.copy(alpha = 0.2f))
        )
    }
}


@Preview
@Composable
fun PreviewPickerWheel() {
    var selectedBpmIndex by remember { mutableStateOf(60) }
    val bpmList = (60..180).map { it.toString() }

    PickerWheel(
        items = bpmList,
        selectedIndex = selectedBpmIndex - 60,
        onItemSelected = { index -> selectedBpmIndex = index + 60 },
        modifier = Modifier.width(100.dp).height(300.dp)
    )
}

@Preview
@Composable
private fun prevControlBar() {
    BasicMusicInfoSet()
}

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    text: String,
    menuItemData: List<String>,
    OnClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Placeholder list of 100 strings for demonstration

    Box(
        modifier = modifier
            .padding(6.dp)
    ) {
        TextButton(
            onClick = { expanded = !expanded }
        ) {
            Text(text = text)
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuItemData.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { OnClick(option) }
                )
            }
        }
    }
}