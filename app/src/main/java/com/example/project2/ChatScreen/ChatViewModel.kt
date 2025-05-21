package com.example.project2.ChatScreen

import android.util.Log
import androidx.compose.animation.core.StartOffsetType.Companion.Delay
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.project2.data.ChatItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.example.project2.data.network.ChatAPI
import com.example.project2.data.network.ChatRequest
import com.example.project2.data.network.NetworkChatItem
import com.google.gson.Gson
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


class ChatViewModel : ViewModel() {
    //private val chatItemExam: ChatItem = ChatItem(id = 0, character = "bot", chatText = "Hello,How can I help you?")
    private val _chatItems = mutableStateOf(listOf<ChatItem>())
    val chatItems: State<List<ChatItem>> get() = _chatItems
    private val chatItem by chatItems
    private val count: Int get() = _chatItems.value.size

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    fun addChatItem(character: String, chatText: String) {
        val chatItem = ChatItem(id = count, character = character, chatText = chatText)
        _chatItems.value = _chatItems.value + chatItem

        sendChatRequest(prompt = chatItem.chatText)
    }


    private fun sendChatRequest(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isGenerating.value = true

                val client = OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build()
                val jsonObject = org.json.JSONObject().apply {
                    put("query", prompt)
                    put("top_k", 4)
                }

                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val request = okhttp3.Request.Builder()
                    .url("https://rag.zongzi.org/api/v1/chat")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    val json = org.json.JSONObject(bodyString ?: "")
                    val answer = json.optString("response", "[无回答]")

                    val chatItem = ChatItem(
                        id = System.currentTimeMillis().toInt(),
                        character = "bot",
                        chatText = answer.trim()
                    )
                    _chatItems.value = _chatItems.value + chatItem
                } else {
                    val chatItem = ChatItem(id = count, character = "bot", chatText = "服务器响应失败")
                    _chatItems.value = _chatItems.value + chatItem
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "网络异常: ${e.message}", e)  // ← 打印异常日志
                val chatItem = ChatItem(id = count, character = "bot", chatText = "network error")
                _chatItems.value = _chatItems.value + chatItem
            } finally {
                _isGenerating.value = false
            }
        }

//    fun getChatItemFromInternet(){
//        try {
//            viewModelScope.launch {
//                fun NetworkChatItem.toChatItem(): ChatItem {
//                    return ChatItem(id = 0 , character = "bot", chatText = this.chatText)
//                }
//
//                val result = ChatAPI.retrofitService.getNetworkChatItem()
//                val resultData = result.map { it.toChatItem() }
//
//                _chatItems.value = resultData
//            }
//        }catch (e: IOException){
//
//        }

    }
}

