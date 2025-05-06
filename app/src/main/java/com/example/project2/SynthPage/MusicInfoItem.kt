package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.launch


@Composable
fun BasicMusicInfoSet(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val musicInfo by viewModel.musicInfo.collectAsState()

    LaunchedEffect(musicInfo) {
        FluidSynthManager.setBasicMusicInfo(musicInfo.BPM, musicInfo.bar, musicInfo.clap)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                        .width(80.dp) // 对应前三个PickerWheel的宽度
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BPM")
                }
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CLAP")
                }
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ROOT")
                }
                Box(
                    modifier = Modifier
                        .width(120.dp) // 对应最后一个PickerWheel的宽度
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SCALE")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PickerWheel(
                    items = (40..240).toList().map { it.toString() },
                    selectedIndex = musicInfo.BPM - 40,
                    onItemSelected = {
                        viewModel.updateBPM(it + 40)
                    },
                    modifier = Modifier.width(80.dp)
                )
                PickerWheel(
                    items = (3..4).toList().map { it.toString() },
                    selectedIndex = musicInfo.clap - 3,
                    onItemSelected = {
                        viewModel.updateClap(it + 3)
                    },
                    modifier = Modifier.width(80.dp)
                )
                val rootItems = listOf("C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B")
                PickerWheel(
                    items = rootItems,
                    selectedIndex = rootItems.indexOf(musicInfo.root),
                    onItemSelected = {
                        viewModel.updateRoot(rootItems[it])
                    },
                    modifier = Modifier.width(80.dp)
                )
                val scaleItems = listOf("major", "minor", "blues", "dorian", "major_pentatonic", "minor_pentatonic", "harmonic_minor", "melodic_minor", "phrygian", "lydian", "mixolydian", "locrian")
                PickerWheel(
                    items = scaleItems,
                    selectedIndex = scaleItems.indexOf(musicInfo.scale),
                    onItemSelected = {
                        viewModel.updateScale(scaleItems[it])
                    },
                    modifier = Modifier.width(120.dp)
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

    Box(modifier = modifier.height(120.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            contentPadding = PaddingValues(vertical = 40.dp), // 上下空出
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                Text(
                    text = item,
                    fontSize = if (index == selectedIndex) 24.sp else 18.sp,
                    color = if (index == selectedIndex) Color.Black else Color.Gray,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                onItemSelected(index)
                            }
                        }
                )
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
