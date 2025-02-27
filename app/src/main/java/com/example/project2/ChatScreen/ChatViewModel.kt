package com.example.project2.ChatScreen

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import java.io.IOException




class ChatViewModel : ViewModel() {
    //private val chatItemExam: ChatItem = ChatItem(id = 0, character = "bot", chatText = "Hello,How can I help you?")
    private val _chatItems = mutableStateOf(listOf<ChatItem>())
    val chatItems: State<List<ChatItem>> get() = _chatItems
    private val chatItem by chatItems
    private val count: Int get() = _chatItems.value.size

    fun addChatItem(character: String, chatText: String) {
        val chatItem = ChatItem(id = count, character = character, chatText = chatText)
        _chatItems.value = _chatItems.value + chatItem

        val options: Map<String, Any> = mapOf(
            "temperature" to 0.7,
            "top_k" to  50,
            "top_p" to  0.9,
        )

        sendChatRequest(model = "deepseek-r1:70b", prompt = chatItem.chatText, options = options)
    }


    private fun sendChatRequest(model: String = "deepseek-r1:70b", prompt: String, options: Map<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fun NetworkChatItem.toChatItem(): ChatItem {
                    return ChatItem(id = count , character = "bot", chatText = this.response)
                }

                val request = ChatRequest(model, prompt, stream = false, options)
                val response = ChatAPI.retrofitService.generateChat(request)
                Log.d("ChatAPI", "Response: ${Gson().toJson(response)}")
                val resultData: ChatItem = response.toChatItem()
                _chatItems.value = _chatItems.value + resultData

            } catch (e: Exception) {
                val chatItem = ChatItem(id = count, character = "bot", chatText = "network error")
                _chatItems.value = _chatItems.value + chatItem
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

