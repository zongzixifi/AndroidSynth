package com.example.project2.MusicGenPage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.data.MusicGeneratedRepository
import com.example.project2.data.UserSessionManager
import com.example.project2.data.database.MusicGenerated
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MusicGenViewModel @Inject constructor(
    private val musicGeneratedRepository: MusicGeneratedRepository,
    private val musicGenRepository: MusicGenRepository
) : ViewModel() {
    private val _generatedMusicUri = MutableStateFlow<Uri?>(null)
    val generatedMusicUri: StateFlow<Uri?> = _generatedMusicUri

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    // 当前会话ID
    private val _currentSessionId = MutableStateFlow<Int?>(UserSessionManager.selectedSessionId)
    val currentSessionId = _currentSessionId.asStateFlow().value?:0

    // 当前会话的生成历史
    @OptIn(ExperimentalCoroutinesApi::class)
    val generatedMusicHistory = _currentSessionId
        .filterNotNull()
        .flatMapLatest { sessionId ->
            musicGeneratedRepository.getGeneratedMusicBySession(sessionId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // 生成状态管理
    private val _generationResult = MutableStateFlow<GenerationResult?>(null)
    val generationResult: StateFlow<GenerationResult?> = _generationResult

    sealed class GenerationResult {
        data class Success(val musicGenerated: MusicGenerated) : GenerationResult()
        data class Error(val message: String) : GenerationResult()
    }

    fun setCurrentSession(sessionId: Int) {
        _currentSessionId.value = sessionId
    }

    fun uploadMusicAndGenerate(
        context: Context,
        musicUri: Uri?,
        description: String,
        durtime: String = "20"
    ) {
        val sessionId = _currentSessionId.value
        if (sessionId == null) {
            _generationResult.value = GenerationResult.Error("请先选择会话")
            return
        }
        Log.d("CheckGeneratedMusicHistory", "current sessionID: ${sessionId}")

        _isGenerating.value = true
        _generatedMusicUri.value = null
        _generationResult.value = null

        viewModelScope.launch {
            try {
                // 准备音频文件（如果有）
                val audioFile = musicUri?.let { uriToFile(context, it) }

                val result = musicGenRepository.generateMusic(
                    description = description,
                    durtime = durtime,
                    audioFile = audioFile
                )

                result.fold(
                    onSuccess = { inputStream ->
                        // 保存音频文件
                        val timestamp = System.currentTimeMillis()
                        val filename = "generated_music_$timestamp.wav"

                        val savedUri = saveAudioToMediaStore(context, inputStream, filename)

                        if (savedUri != null) {
                            _generatedMusicUri.value = savedUri

                            // 保存到数据库
                            val musicGenerated = MusicGenerated(
                                sessionId = currentSessionId,
                                url = savedUri.toString(),
                                prompt = description
                            )

                            saveGeneratedMusicToDatabase(musicGenerated)

                            _generationResult.value = GenerationResult.Success(musicGenerated)
                            Log.d("MusicGenViewModel", "音乐生成成功并已保存到历史记录")
                        } else {
                            _generationResult.value = GenerationResult.Error("保存音频文件失败")
                        }
                    },
                    onFailure = { exception ->
                        _generationResult.value = GenerationResult.Error(
                            exception.message ?: "生成失败"
                        )
                    }
                )

                // 清理临时文件
                audioFile?.delete()
            } catch (e: Exception) {
                Log.e("MusicGenViewModel", "音乐生成失败", e)
                _generationResult.value = GenerationResult.Error("生成失败: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * 保存音频到媒体存储
     */
    private suspend fun saveAudioToMediaStore(
        context: Context,
        inputStream: InputStream,
        filename: String
    ): Uri? = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { generatedUri ->
                resolver.openOutputStream(generatedUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                generatedUri
            }
        } catch (e: Exception) {
            Log.e("MusicGenViewModel", "保存音频失败", e)
            null
        }
    }

    /**
     * 保存生成的音乐信息到数据库
     */
    private suspend fun saveGeneratedMusicToDatabase(musicGenerated: MusicGenerated) {
        try {
            musicGeneratedRepository.insertGeneratedMusic(musicGenerated)
            Log.d("MusicGenViewModel", "音乐信息已保存到数据库")
        } catch (e: Exception) {
            Log.e("MusicGenViewModel", "保存到数据库失败", e)
        }
    }

    /**
     * 删除指定的生成音乐
     */
    fun deleteGeneratedMusic(music: MusicGenerated, context: Context) {
        viewModelScope.launch {
            try {
                // 从数据库删除记录
                musicGeneratedRepository.deleteMusic(music)

                // 尝试从媒体存储删除文件
                try {
                    val uri = Uri.parse(music.url)
                    context.contentResolver.delete(uri, null, null)
                    Log.d("MusicGenViewModel", "音频文件已从媒体存储删除")
                } catch (e: Exception) {
                    Log.w("MusicGenViewModel", "删除音频文件失败，但数据库记录已删除", e)
                }

                Log.d("MusicGenViewModel", "生成音乐删除成功")
            } catch (e: Exception) {
                Log.e("MusicGenViewModel", "删除生成音乐失败", e)
            }
        }
    }

    /**
     * 清空当前会话的所有生成音乐
     */
    fun clearCurrentSessionMusic(context: Context) {
        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            try {
                // 获取当前会话的所有音乐，准备删除文件
                val musicList = musicGeneratedRepository.getGeneratedMusicBySession(sessionId).first()

                // 删除数据库记录
                musicGeneratedRepository.deleteMusicBySession(sessionId)

                // 删除音频文件
                musicList.forEach { music ->
                    try {
                        val uri = Uri.parse(music.url)
                        context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        Log.w("MusicGenViewModel", "删除音频文件失败: ${music.url}", e)
                    }
                }

                Log.d("MusicGenViewModel", "当前会话的所有生成音乐已清空")
            } catch (e: Exception) {
                Log.e("MusicGenViewModel", "清空会话音乐失败", e)
            }
        }
    }

    /**
     * 清除生成结果状态
     */
    fun clearGenerationResult() {
        _generationResult.value = null
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