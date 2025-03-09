package com.example.project2.MusicGenPage

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MusicGenViewModel : ViewModel() {

    private val _generatedMusicUri = MutableStateFlow<Uri?>(null)
    val generatedMusicUri: StateFlow<Uri?> = _generatedMusicUri

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val client = OkHttpClient()

    fun uploadMusicAndGenerate(context: Context, musicUri: Uri) {
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(context, musicUri)
                val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val request = Request.Builder()
                    .url("https://your-model-endpoint.com/generate")
                    .post(multipartBody.body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val generatedFile = File(context.cacheDir, "generated_music.wav")
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(generatedFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    _generatedMusicUri.value = Uri.fromFile(generatedFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "input_audio.wav")
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}

@Composable
fun MusicGenerationScreen(context: Context) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var generatedMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var generatedIsPlaying by remember { mutableStateOf(false) }
    val viewModel: MusicGenViewModel = viewModel()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // **输入区域标题**
        Text(
            text = "Input",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // **导入音乐按钮**
        Button(
            onClick = { musicPickerLauncher.launch("audio/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text(text = "Import your raw music", color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.Folder, contentDescription = "Import", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **播放音乐的进度条**
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f),
                color = Color.Black,
            )
            IconButton(onClick = {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // **文本输入框**
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Describe your music") },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // **提交按钮**
        Button(
            onClick = {
                viewModel.uploadMusicAndGenerate(context, musicUri!!)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Submit and generate", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **进度条**
        if (viewModel.isGenerating.collectAsState().value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Generating...", fontSize = 14.sp, fontStyle = FontStyle.Italic)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **模型生成的音频文件展示**
        if (viewModel.generatedMusicUri.collectAsState().value != null) {
            val generatedUri = viewModel.generatedMusicUri.collectAsState().value
            if (generatedMediaPlayer == null && generatedUri != null) {
                generatedMediaPlayer = MediaPlayer().apply {
                    setDataSource(context, generatedUri)
                    prepare()
                }
            }

            Text(
                text = "Generated Music Output",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { generatedProgress },
                    modifier = Modifier.weight(1f),
                    color = Color.Blue,
                )
                IconButton(onClick = {
                    generatedMediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            generatedIsPlaying = false
                        } else {
                            player.start()
                            generatedIsPlaying = true
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (generatedIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MusicGenerationScreenPrev() {
    val context = LocalContext.current
    MusicGenerationScreen(context)
}