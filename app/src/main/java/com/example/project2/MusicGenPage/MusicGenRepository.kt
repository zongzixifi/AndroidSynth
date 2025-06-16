package com.example.project2.MusicGenPage

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//
//class MusicGenRepository @Inject constructor() {
//
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(180, TimeUnit.SECONDS)
//        .readTimeout(180, TimeUnit.SECONDS)
//        .writeTimeout(180, TimeUnit.SECONDS)
//        .build()
//
//    /**
//     * 生成音乐请求
//     * @param description 音乐描述
//     * @param durtime 时长（秒）
//     * @param audioFile 可选的参考音频文件
//     * @return 生成的音频数据流
//     */
//    suspend fun generateMusic(
//        description: String,
//        durtime: String = "20",
//        audioFile: File? = null
//    ): Result<InputStream> = withContext(Dispatchers.IO) {
//        try {
//            val url = "http://backend.zongzi.org/api/user/generate"
//            //val url = "http://192.168.31.9:6006/generate"
//
//            val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("dur_time", durtime)
//                .addFormDataPart("description", description)
//
//            // 如果有参考音频文件
//            audioFile?.let { file ->
//                val mimeType = getMimeType(file) ?: "audio/wav"
//                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
//                requestBuilder.addFormDataPart("melody", file.name, requestBody)
//            }
//
//            val request = Request.Builder()
//                .url(url)
//                .post(requestBuilder.build())
//                .build()
//
//            Log.d("MusicGenRepository", "开始生成音乐请求...")
//            val response = client.newCall(request).execute()
//            Log.d("MusicGenRepository", "Response received, code: ${response.code}")
//
//            when {
//                response.isSuccessful && response.body != null -> {
//                    Log.d("MusicGenRepository", "音频数据成功返回")
//                    Result.success(response.body!!.byteStream())
//                }
//                response.body == null -> {
//                    Log.e("MusicGenRepository", "服务器返回空响应")
//                    Result.failure(Exception("服务器返回空响应"))
//                }
//                else -> {
//                    Log.e("MusicGenRepository", "生成失败: HTTP ${response.code}")
//                    Result.failure(Exception("生成失败: HTTP ${response.code}"))
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("MusicGenRepository", "音乐生成请求失败", e)
//            Result.failure(e)
//        }
//    }
//
//    /**
//     * 获取文件的 MIME 类型
//     */
//    private fun getMimeType(file: File): String? {
//        val extension = file.extension.lowercase()
//        return when (extension) {
//            "wav" -> "audio/wav"
//            "mp3" -> "audio/mpeg"
//            "m4a" -> "audio/mp4"
//            "aac" -> "audio/aac"
//            "flac" -> "audio/flac"
//            "ogg" -> "audio/ogg"
//            else -> "audio/*"
//        }
//    }
//}

// 1. 数据类定义
@Serializable
data class GenerateTaskResponse(
    val taskId: String,
    val status: String, // "pending", "processing", "completed", "failed"
    val message: String? = null
)

@Serializable
data class TaskStatusResponse(
    val taskId: String,
    val status: String,
    val progress: Int? = null, // 0-100
    val audioUrl: String? = null,
    val message: String? = null
)

class MusicGenRepository @Inject constructor() {
    val baseURL = "backend.zongzi.org"
    //val baseURL = "192.168.31.1:8081"

    private val json = Json { ignoreUnknownKeys = true }

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

    private val client = OkHttpClient.Builder()
    .connectTimeout(180, TimeUnit.SECONDS)
    .readTimeout(180, TimeUnit.SECONDS)
    .writeTimeout(180, TimeUnit.SECONDS)
    .build()

    suspend fun submitGenerateTask(
        description: String,
        durtime: String = "20",
        audioFile: File? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://$baseURL/api/user/generate/submit"

            Log.d("MusicGenRepository", "🎵 开始提交任务")

            // 检查文件
            audioFile?.let { file ->
                Log.d("MusicGenRepository", "🎵 文件信息:")
                Log.d("MusicGenRepository", "  - 名称: ${file.name}")
                Log.d("MusicGenRepository", "  - 大小: ${file.length()} bytes")
                Log.d("MusicGenRepository", "  - 扩展名: ${file.extension}")

                // 限制文件大小（Cloudflare友好）
                val maxSizeMB = 25 // 保守一点，25MB
                if (file.length() > maxSizeMB * 1024 * 1024) {
                    return@withContext Result.failure(Exception("文件太大，请选择小于${maxSizeMB}MB的文件"))
                }
            }

            val requestBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("dur_time", durtime)
                .addFormDataPart("description", description)

            // 优化文件处理
            audioFile?.let { file ->
                try {
                    // 使用更通用的MIME类型
                    val mimeType = when (file.extension.lowercase()) {
                        "wav" -> "audio/wav"
                        "mp3" -> "audio/mpeg"
                        "m4a" -> "audio/mp4"
                        "aac" -> "audio/aac"
                        else -> "audio/wav" // 默认
                    }

                    Log.d("MusicGenRepository", "🎵 使用MIME类型: $mimeType")

                    val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

                    // 使用简单的文件名，避免特殊字符
                    val safeFileName = "audio.${file.extension.lowercase()}"
                    requestBuilder.addFormDataPart("melody", safeFileName, requestBody)

                    Log.d("MusicGenRepository", "🎵 文件已添加: $safeFileName")

                } catch (e: Exception) {
                    Log.e("MusicGenRepository", "❌ 文件处理失败", e)
                    return@withContext Result.failure(Exception("文件处理失败: ${e.message}"))
                }
            }

            // 为文件上传优化的客户端设置
            val uploadClient = client.newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)  // 10分钟写入超时
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBuilder.build())
                // 添加Cloudflare友好的头部
                .header("User-Agent", "MusicGenApp/1.0")
                .header("Accept", "application/json")
                .build()

            Log.d("MusicGenRepository", "🎵 发送请求...")
            val response = uploadClient.newCall(request).execute()

            Log.d("MusicGenRepository", "🎵 响应状态: ${response.code}")
            Log.d("MusicGenRepository", "🎵 响应头: ${response.headers}")

            when (response.code) {
                200 -> {
                    val responseBody = response.body!!.string()
                    Log.d("MusicGenRepository", "🎵 响应内容: $responseBody")

                    val taskResponse = json.decodeFromString<GenerateTaskResponse>(responseBody)
                    Result.success(taskResponse.taskId)
                }
                413 -> {
                    Result.failure(Exception("文件太大，请选择更小的音频文件"))
                }
                415 -> {
                    Result.failure(Exception("不支持的文件格式，请使用WAV、MP3或M4A格式"))
                }
                502 -> {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("MusicGenRepository", "❌ 502错误，响应: $errorBody")
                    Result.failure(Exception("服务器暂时不可用，请稍后重试"))
                }
                else -> {
                    val errorBody = response.body?.string() ?: "无错误详情"
                    Log.e("MusicGenRepository", "❌ HTTP ${response.code}: $errorBody")
                    Result.failure(Exception("请求失败: HTTP ${response.code}"))
                }
            }

        } catch (e: Exception) {
            Log.e("MusicGenRepository", "❌ 提交任务异常", e)
            Result.failure(e)
        }
    }

    /**
     * 第一步：提交生成任务
     */
    suspend fun submitGenerateTask1(
        description: String,
        durtime: String = "20",
        audioFile: File? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://$baseURL/api/user/generate/submit"

            val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("dur_time", durtime)
                .addFormDataPart("description", description)

            audioFile?.let { file ->
                val mimeType = getMimeType(file) ?: "audio/wav"
                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                requestBuilder.addFormDataPart("melody", file.name, requestBody)
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBuilder.build())
                .build()

            Log.d("MusicGenRepository", "提交音乐生成任务...")
            val response = client.newCall(request).execute()

            when {
                response.isSuccessful && response.body != null -> {
                    val responseBody = response.body!!.string()
                    val taskResponse = json.decodeFromString<GenerateTaskResponse>(responseBody)
                    Log.d("MusicGenRepository", "任务提交成功，ID: ${taskResponse.taskId}")
                    Result.success(taskResponse.taskId)
                }
                else -> {
                    Log.e("MusicGenRepository", "任务提交失败: HTTP ${response.code}")
                    Result.failure(Exception("任务提交失败: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGenRepository", "提交任务请求失败", e)
            Result.failure(e)
        }
    }

    /**
     * 第二步：轮询任务状态
     */
    suspend fun checkTaskStatus(taskId: String): Result<TaskStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "https://$baseURL/api/user/generate/status/$taskId"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            when {
                response.isSuccessful && response.body != null -> {
                    val responseBody = response.body!!.string()
                    val statusResponse = json.decodeFromString<TaskStatusResponse>(responseBody)
                    Result.success(statusResponse)
                }
                else -> {
                    Result.failure(Exception("状态查询失败: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGenRepository", "状态查询失败", e)
            Result.failure(e)
        }
    }

    /**
     * 第三步：下载完成的音频
     */
    suspend fun downloadAudio1(audioUrl: String): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = if (audioUrl.startsWith("http")) {
                audioUrl  // 已经是完整URL
            } else {
                "https://$baseURL/api/user$audioUrl"  // 补全域名
            }

            val request = Request.Builder()
                .url(fullUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()

            when {
                response.isSuccessful && response.body != null -> {
                    Result.success(response.body!!.byteStream())
                }
                else -> {
                    Result.failure(Exception("音频下载失败: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGenRepository", "音频下载失败", e)
            Result.failure(e)
        }
    }

    suspend fun downloadAudio(audioUrl: String): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = if (audioUrl.startsWith("http")) {
                audioUrl
            } else {
                "https://$baseURL/api/user$audioUrl"
            }

            Log.d("MusicGenRepository", "🔍 开始下载音频")
            Log.d("MusicGenRepository", "🔍 原始URL: $audioUrl")
            Log.d("MusicGenRepository", "🔍 完整URL: $fullUrl")

            val request = Request.Builder()
                .url(fullUrl)
                .header("Accept", "audio/wav, audio/*, */*")
                .header("Cache-Control", "no-cache")
                .get()
                .build()

            val response = client.newCall(request).execute()

            Log.d("MusicGenRepository", "🔍 响应状态码: ${response.code}")
            Log.d("MusicGenRepository", "🔍 响应消息: ${response.message}")

            // 打印所有响应头
            response.headers.forEach { (name, value) ->
                Log.d("MusicGenRepository", "🔍 响应头 $name: $value")
            }

            when {
                response.isSuccessful && response.body != null -> {
                    val contentType = response.header("Content-Type")
                    val contentLength = response.header("Content-Length")

                    Log.d("MusicGenRepository", "🔍 Content-Type: $contentType")
                    Log.d("MusicGenRepository", "🔍 Content-Length: $contentLength")

                    val body = response.body!!
                    val bodyBytes = body.bytes()

                    Log.d("MusicGenRepository", "🔍 实际下载字节数: ${bodyBytes.size}")

                    // 检查前几个字节，判断文件类型
                    if (bodyBytes.size >= 12) {
                        val header = bodyBytes.sliceArray(0..11)
                        val headerString = header.joinToString(" ") { "%02x".format(it) }
                        Log.d("MusicGenRepository", "🔍 文件头 (hex): $headerString")

                        // 检查是否是WAV文件 (RIFF...WAVE)
                        val riffSignature = bodyBytes.sliceArray(0..3)
                        val waveSignature = bodyBytes.sliceArray(8..11)
                        val riffString = String(riffSignature)
                        val waveString = String(waveSignature)

                        Log.d("MusicGenRepository", "🔍 RIFF签名: $riffString")
                        Log.d("MusicGenRepository", "🔍 WAVE签名: $waveString")

                        if (riffString == "RIFF" && waveString == "WAVE") {
                            Log.d("MusicGenRepository", "✅ WAV文件格式验证通过")
                        } else {
                            Log.w("MusicGenRepository", "⚠️ 可能不是WAV文件")
                            // 检查是否是HTML错误页面
                            val contentStart = String(bodyBytes.sliceArray(0..minOf(100, bodyBytes.size - 1)))
                            Log.d("MusicGenRepository", "🔍 内容开头: $contentStart")
                        }
                    } else {
                        Log.e("MusicGenRepository", "❌ 文件太小，只有 ${bodyBytes.size} 字节")
                    }

                    if (bodyBytes.isEmpty()) {
                        Log.e("MusicGenRepository", "❌ 下载的内容为空")
                        Result.failure(Exception("下载的音频文件为空"))
                    } else {
                        Log.d("MusicGenRepository", "✅ 音频下载成功，返回ByteArrayInputStream")
                        Result.success(ByteArrayInputStream(bodyBytes))
                    }
                }
                else -> {
                    Log.e("MusicGenRepository", "❌ HTTP错误: ${response.code}")
                    response.body?.let { errorBody ->
                        val errorContent = errorBody.string()
                        Log.e("MusicGenRepository", "❌ 错误内容: $errorContent")
                    }
                    Result.failure(Exception("音频下载失败: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGenRepository", "❌ 下载异常", e)
            Result.failure(e)
        }
    }


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
        audioFile: File? = null,
        onProgress: (Int) -> Unit = {}
    ): Result<InputStream> {

        // 1. 提交任务
        val taskResult = submitGenerateTask(description, durtime, audioFile)
        if (taskResult.isFailure) {
            return Result.failure(taskResult.exceptionOrNull()!!)
        }

        val taskId = taskResult.getOrNull()!!
        Log.d("MusicGenRepository", "开始轮询任务状态: $taskId")

        // 2. 轮询状态
        var attempts = 0
        val maxAttempts = 120 // 最多轮询2分钟 (每次1秒)

        while (attempts < maxAttempts) {
            delay(1000) // 每秒查询一次
            attempts++

            val statusResult = checkTaskStatus(taskId)
            if (statusResult.isFailure) {
                Log.w("MusicGenRepository", "状态查询失败，继续重试...")
                continue
            }

            val status = statusResult.getOrNull()!!
            Log.d("MusicGenRepository", "任务状态: ${status.status}, 进度: ${status.progress}")

            // 更新进度
            status.progress?.let { onProgress(it) }

            when (status.status) {
                "completed" -> {
                    if (status.audioUrl != null) {
                        Log.d("MusicGenRepository", "任务完成，开始下载音频")
                        return downloadAudio(status.audioUrl)
                    } else {
                        return Result.failure(Exception("任务完成但没有音频URL"))
                    }
                }
                "failed" -> {
                    val errorMsg = status.message ?: "任务处理失败"
                    Log.e("MusicGenRepository", "任务失败: $errorMsg")
                    return Result.failure(Exception(errorMsg))
                }
                "pending", "processing" -> {
                    // 继续等待
                    continue
                }
                else -> {
                    Log.w("MusicGenRepository", "未知状态: ${status.status}")
                    continue
                }
            }
        }

        return Result.failure(Exception("任务轮询超时"))
    }
}
