package com.example.project2.MusicGenPage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit


class MusicGenViewModel : ViewModel() {

    private val _generatedMusicUri = MutableStateFlow<Uri?>(null)
    val generatedMusicUri: StateFlow<Uri?> = _generatedMusicUri

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.SECONDS)
        .readTimeout(5000, TimeUnit.SECONDS)
        .writeTimeout(3000, TimeUnit.SECONDS)
        .build()

    fun uploadMusicAndGenerate(context: Context, musicUri: Uri?, prompt: String) {
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "http://207.148.111.64:12525/funk_generate"  // Android 访问本机的方式
                _generatedMusicUri.value = null
                //val jsonPrompt = """{"prompt": "$prompt"}"""
                //val jsonRequestBody = jsonPrompt.toRequestBody("application/json".toMediaType())
                val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("prompt", prompt)  // 添加 prompt

                // **如果用户上传了音频**
                musicUri?.let {
                    val file = uriToFile(context, it)
                    run {
                        val requestBody = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                        requestBuilder.addFormDataPart("audio", file.name, requestBody)
                    }
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestBuilder.build())
                    .build()



                val response = client.newCall(request).execute()

                Log.d("MusicGenViewModel", "Response received, code: ${response.code}")


                if (response.body == null) {
                    Log.e("MusicGenViewModel", "Received empty response body from server")
                    return@launch
                }

                if (response.isSuccessful) {
                    Log.d("MusicGenViewModel", "音频数据成功返回，正在保存...")
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, "generated_music.wav")
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                        put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let { generatedUri ->
                        resolver.openOutputStream(generatedUri)?.use { outputStream ->
                            response.body?.byteStream()?.copyTo(outputStream)
                        }
                        _generatedMusicUri.value = generatedUri
                    }
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