package com.example.project2.MusicGenPage

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MusicGenRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    /**
     * 生成音乐请求
     * @param description 音乐描述
     * @param durtime 时长（秒）
     * @param audioFile 可选的参考音频文件
     * @return 生成的音频数据流
     */
    suspend fun generateMusic(
        description: String,
        durtime: String = "20",
        audioFile: File? = null
    ): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            //val url = "https://musicgen.zongzi.org/generate"
            val url = "http://192.168.31.9:6006/generate"

            val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("dur_time", durtime)
                .addFormDataPart("description", description)

            // 如果有参考音频文件
            audioFile?.let { file ->
                val mimeType = getMimeType(file) ?: "audio/wav"
                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                requestBuilder.addFormDataPart("melody", file.name, requestBody)
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBuilder.build())
                .build()

            Log.d("MusicGenRepository", "开始生成音乐请求...")
            val response = client.newCall(request).execute()
            Log.d("MusicGenRepository", "Response received, code: ${response.code}")

            when {
                response.isSuccessful && response.body != null -> {
                    Log.d("MusicGenRepository", "音频数据成功返回")
                    Result.success(response.body!!.byteStream())
                }
                response.body == null -> {
                    Log.e("MusicGenRepository", "服务器返回空响应")
                    Result.failure(Exception("服务器返回空响应"))
                }
                else -> {
                    Log.e("MusicGenRepository", "生成失败: HTTP ${response.code}")
                    Result.failure(Exception("生成失败: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGenRepository", "音乐生成请求失败", e)
            Result.failure(e)
        }
    }

    /**
     * 获取文件的 MIME 类型
     */
    private fun getMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "wav" -> "audio/wav"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            else -> "audio/*"
        }
    }
}
