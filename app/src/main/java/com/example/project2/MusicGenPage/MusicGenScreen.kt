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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.R

@Composable
fun MusicGenerationScreen(context: Context, viewModel: MusicGenViewModel, modifier: Modifier=Modifier) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var durtime by remember { mutableStateOf(20) }
    var generatedMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var generatedIsPlaying by remember { mutableStateOf(false) }

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
        // 输入区域标题
        val pressStartFont = FontFamily(Font(R.font.pressstart_2p_regular))
        Text(
            text = stringResource(R.string.input),
            fontSize = 28.sp,
            fontFamily = pressStartFont,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)
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
            value = textInput,
            onValueChange = { textInput = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GenerateButton(
            onClick = {
                if (musicUri != null || textInput.isNotEmpty()) {
                    viewModel.uploadMusicAndGenerate(context, musicUri, textInput, durtime.toString())
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_select_a_music_file_or_make_text_input_first),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 进度条
        IndeterminateIndicator(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.generatedMusicUri.collectAsState().value != null) {
            val generatedUri = viewModel.generatedMusicUri.collectAsState().value
            if (generatedMediaPlayer == null && generatedUri != null) {
                generatedMediaPlayer = MediaPlayer().apply {
                    setDataSource(context, generatedUri)
                    prepare()
                }
            }
            GeneratedAudioPlayer(
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
        modifier = modifier.fillMaxWidth().padding(bottom = 10.dp),
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

@Preview
@Composable
private fun MusicGenerationScreenPrev() {
    val fakeViewModel : MusicGenViewModel = viewModel()
    val context = LocalContext.current
    MusicGenerationScreen(context, fakeViewModel)
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
            .height(150.dp),
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
// endregion