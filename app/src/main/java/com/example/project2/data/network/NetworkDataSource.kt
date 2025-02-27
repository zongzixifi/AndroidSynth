package com.example.project2.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


interface NetworkDataSource {
//    @GET("api/generate")
//    suspend fun getNetworkChatItem(): List<NetworkChatItem>

    @POST("api/generate")
    suspend fun generateChat(@Body request: ChatRequest): NetworkChatItem
    //suspend fun pushNetworkChatItem(): List<NetworkChatItem>
}

object ChatAPI{
    private const val BASE_URL = "http://172.25.54.61:11434/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时 30s
            .readTimeout(300, TimeUnit.SECONDS)    // 读取超时 60s
            .writeTimeout(300, TimeUnit.SECONDS)   // 写入超时 60s
            .build()
    }

    val retrofitService :NetworkDataSource by lazy{
        retrofit.create(NetworkDataSource::class.java)
    }
}