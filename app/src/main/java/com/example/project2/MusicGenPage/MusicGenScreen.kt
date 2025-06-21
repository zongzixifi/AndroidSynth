package com.example.project2.MusicGenPage

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.project2.R
import com.example.project2.SynthPage.SimpleIconButton
import com.example.project2.data.database.MusicGenerated
import kotlinx.coroutines.delay

@Composable
fun MusicGenerationScreen(
    context: Context,
    viewModel: MusicGenViewModel = hiltViewModel(),
    modifier: Modifier=Modifier,
    onClickJumpFrontScreen: () -> Unit ={}
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var durtime by remember { mutableStateOf(20) }
    var generatedMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var generatedIsPlaying by remember { mutableStateOf(false) }

    var selectedMusicUri by remember { mutableStateOf<Uri?>(null) }
    val musicHistory by viewModel.generatedMusicHistory.collectAsState()
    var description by remember { mutableStateOf<String>("") }

    var progress by remember { mutableStateOf(0.0f) }
    var generatedProgress by remember { mutableStateOf(0.0f) }

    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            musicUri = it
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, it)
                prepare()
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                progress = player.currentPosition / player.duration.toFloat()
            }
            delay(500) // 每 500ms 更新一次进度
        }
    }

    LaunchedEffect(generatedIsPlaying) {
        while (generatedIsPlaying) {
            generatedMediaPlayer?.let { player ->
                generatedProgress = player.currentPosition / player.duration.toFloat()
            }
            delay(500) // 每 500ms 更新一次进度
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleIconButton(
            modifier =Modifier.align(Alignment.Start),
            onClick = onClickJumpFrontScreen,
            icon = Icons.Filled.ArrowBackIosNew
        )
        // 输入区域标题
        val pressStartFont = FontFamily(Font(R.font.pressstart_2p_regular))
        Text(
            text = stringResource(R.string.input),
            fontSize = 28.sp,
            fontFamily = pressStartFont,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ImportAudioButton(
            onClick = { musicPickerLauncher.launch("audio/*") },
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AudioPlaybackBar(
            modifier =  Modifier
                .weight(1f)
                .padding(12.dp),
            progress = progress,
            isPlaying = isPlaying,
            onPlayPauseClick = {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        DurationSlider(
            value = durtime,
            onValueChange = { durtime = it }
        )
        DescriptionInputField(
            modifier = Modifier.weight(1f),
            value = description,
            onValueChange = { description = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GenerateButton(
            onClick = {
                if (musicUri != null || description.isNotEmpty()) {
                    viewModel.uploadMusicAndGenerate(context, musicUri, description, durtime.toString())
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_select_a_music_file_or_make_text_input_first),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 进度条
        IndeterminateIndicator(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // 生成记录 选择、回播
        MusicHistoryLazyColum(
            modifier = Modifier.weight(2f),
            musicHistory = musicHistory,
            onMusicSelected = { selectedUrl ->
                selectedMusicUri = Uri.parse(selectedUrl)
                val selectedMusic = musicHistory.find { it.url == selectedUrl }
                selectedMusic?.let { music ->
                    description = music.prompt // 自动填充描述
                }
            }
        )

        LaunchedEffect(selectedMusicUri) {
            selectedMusicUri?.let {
                generatedMediaPlayer?.release()
                generatedMediaPlayer = MediaPlayer().apply {
                    setDataSource(context, it)
                    prepare()
                }
            }
        }
        GeneratedAudioPlayer(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.generated_music_output),
            progress = generatedProgress,
            isPlaying = generatedIsPlaying,
            onPlayPauseClick = {
                generatedMediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        generatedIsPlaying = false
                    } else {
                        player.start()
                        generatedIsPlaying = true
                    }
                }
            }
        )

    }
}

@Composable
fun IndeterminateIndicator(viewModel: MusicGenViewModel) {
    if (!viewModel.isGenerating.collectAsState().value) return

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = Color.Blue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Generating...", fontSize = 14.sp, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun DurationSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 10..60,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value 秒",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.LightGray,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(4.dp)
        )
    }
}
// region: 封装的 Composable 组件

@Composable
fun ImportAudioButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Text(text = stringResource(R.string.import_your_raw_music), color = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Filled.Folder, contentDescription = "Import", tint = Color.Black)
    }
}

@Composable
fun AudioPlaybackBar(
    progress: Float,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f),
            color = Color.Black,
        )
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause"
            )
        }
    }
}

@Composable
fun DescriptionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        placeholder = { Text(stringResource(R.string.describe_your_music)) },
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun GenerateButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
    ) {
        Text(text = stringResource(R.string.submit_and_generate), color = Color.White)
    }
}

@Composable
fun GeneratedAudioPlayer(
    title: String,
    progress: Float,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f),
                color = Color.Blue,
            )
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }
    }
}

@Composable
fun MusicHistoryLazyColum(
    musicHistory: List<MusicGenerated>,
    onMusicSelected: (String) -> Unit, // 返回选中的音频文件 URL
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "历史记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (musicHistory.isNotEmpty()) {
                Text(
                    text = "${musicHistory.size} 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (musicHistory.isEmpty()) {
            // 空状态
            EmptyHistoryRow()
        } else {
            // 历史记录横向列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = musicHistory,
                    key = { it.music_id }
                ) { music ->
                    MusicHistoryCard(
                        music = music,
                        onSelected = { onMusicSelected(music.url) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MusicHistoryCardPrev() {
    val sampleMusic = MusicGenerated(
        music_id = 1,
        sessionId = 1,
        url = "https://example.com/sample_music.wav",
        prompt = "轻快的流行音乐，带有电子合成器和鼓点，适合运动时听"
    )

    MusicHistoryCard(
        music = sampleMusic,
        onSelected = { /* Preview 中的空实现 */ }
    )
}

@Composable
private fun MusicHistoryCard(
    music: MusicGenerated,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 顶部：音乐图标和ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "#${music.music_id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prompt 文本
            Text(
                text = music.prompt,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(30.dp) // 固定高度保持卡片一致
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 选择按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSelected,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "选择",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicOff,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "暂无历史记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "生成音乐后会在这里显示",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}